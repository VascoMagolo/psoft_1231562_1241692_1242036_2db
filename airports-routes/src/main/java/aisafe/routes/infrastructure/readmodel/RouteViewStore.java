package aisafe.routes.infrastructure.readmodel;

import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteStatus;
import aisafe.shared.application.Versioned;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * The read model of the routes (CQRS): table {@code route_view}, one row per route of every
 * replica, written only from route events and from the {@link RouteViewReconciler}. The write
 * model (table {@code route}) stays partitioned between the replicas.
 *
 * <p>Each row keeps the stamp of the change it shows. A change is applied only if it is later
 * than that one, the same rule ("last writer wins") as the replication, so events and
 * reconciliation can arrive in any order. A deleted route stays as a row marked deleted, so an
 * older event cannot bring it back.</p>
 */
@Component
public class RouteViewStore {

    private final JdbcTemplate jdbc;

    public RouteViewStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @param key  {@code ORIGIN-DESTINATION}
     * @param view the route after the change, null if the change deleted it
     * @return whether the change was applied (false: this row already shows a later change)
     */
    @Transactional
    public boolean apply(String key, RouteView view, Long version, long stamp, String stampNode) {
        String[] codes = key.split("-", 2);
        List<Map<String, Object>> current = jdbc.queryForList(
                "SELECT stamp, stamp_node FROM route_view WHERE origin_iata_code = ? AND destination_iata_code = ?",
                codes[0], codes[1]);
        if (!current.isEmpty() && !isLater(stamp, stampNode,
                ((Number) current.get(0).get("stamp")).longValue(), (String) current.get(0).get("stamp_node"))) {
            return false;
        }
        boolean deleted = view == null;
        Object[] values = {
                deleted ? null : view.estimatedFlightTime(), deleted ? null : view.minimumRange(),
                deleted ? null : view.minimumCapacity(), deleted ? null : view.status().name(),
                version, stamp, stampNode == null ? "" : stampNode, deleted, codes[0], codes[1]};
        if (current.isEmpty()) {
            jdbc.update("INSERT INTO route_view (estimated_flight_time, minimum_range, minimum_capacity, status, "
                    + "version, stamp, stamp_node, deleted, origin_iata_code, destination_iata_code) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", values);
        } else {
            jdbc.update("UPDATE route_view SET estimated_flight_time = ?, minimum_range = ?, minimum_capacity = ?, "
                    + "status = ?, version = ?, stamp = ?, stamp_node = ?, deleted = ? "
                    + "WHERE origin_iata_code = ? AND destination_iata_code = ?", values);
        }
        return true;
    }

    /** Every route that is not deleted, with its version, ordered by origin and destination. */
    public List<Versioned<Route>> findAll() {
        return jdbc.query("SELECT * FROM route_view WHERE deleted = FALSE "
                        + "ORDER BY origin_iata_code, destination_iata_code",
                (rs, row) -> new Versioned<>(new RouteView(
                        rs.getString("origin_iata_code"), rs.getString("destination_iata_code"),
                        rs.getInt("estimated_flight_time"), rs.getDouble("minimum_range"),
                        rs.getInt("minimum_capacity"), RouteStatus.valueOf(rs.getString("status"))).toRoute(),
                        rs.getObject("version", Long.class)));
    }

    /** Later time; with the same time, the higher replica id (as the replication does). */
    static boolean isLater(long stamp, String node, long otherStamp, String otherNode) {
        int byTime = Long.compare(stamp, otherStamp);
        return byTime != 0 ? byTime > 0 : (node == null ? "" : node).compareTo(otherNode == null ? "" : otherNode) > 0;
    }
}
