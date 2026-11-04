package aisafe.airportsroutes.infrastructure.readmodel;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * The read model (CQRS): table {@code read_model}, one row per entity of every replica (routes,
 * airports, flight copies), with the view the queries need as JSON. Written only from events and
 * from the {@link ReadModelReconciler}; the write model (the tables of each entity) stays as in
 * Assignment 1, partitioned between the replicas.
 *
 * <p>Each row keeps the stamp of the change it shows. A change is applied only if it is later
 * than that one, the same rule ("last writer wins") as the replication, so events and
 * reconciliation can arrive in any order. A deleted entity stays as a row marked deleted, so an
 * older event cannot bring it back.</p>
 */
@Component
public class ReadModelStore {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public ReadModelStore(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    /**
     * @param view the entity after the change, null if the change deleted it
     * @return whether the change was applied (false: this row already shows a later change)
     */
    @Transactional
    public boolean apply(String type, String key, JsonNode view, Long version, long stamp, String stampNode) {
        List<Map<String, Object>> current = jdbc.queryForList(
                "SELECT stamp, stamp_node FROM read_model WHERE entity_type = ? AND entity_key = ?", type, key);
        if (!current.isEmpty() && !isLater(stamp, stampNode,
                ((Number) current.get(0).get("stamp")).longValue(), (String) current.get(0).get("stamp_node"))) {
            return false;
        }
        boolean deleted = view == null;
        Object[] values = {deleted ? null : objectMapper.writeValueAsString(view), version, stamp,
                stampNode == null ? "" : stampNode, deleted, type, key};
        if (current.isEmpty()) {
            jdbc.update("INSERT INTO read_model (content, version, stamp, stamp_node, deleted, entity_type, entity_key) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)", values);
        } else {
            jdbc.update("UPDATE read_model SET content = ?, version = ?, stamp = ?, stamp_node = ?, deleted = ? "
                    + "WHERE entity_type = ? AND entity_key = ?", values);
        }
        return true;
    }

    /** Every entity of this type that is not deleted, ordered by key. */
    public List<Row> findAll(String type) {
        return jdbc.query("SELECT entity_key, content, version FROM read_model WHERE entity_type = ? AND deleted = FALSE "
                        + "ORDER BY entity_key",
                (rs, n) -> new Row(rs.getString("entity_key"), objectMapper.readTree(rs.getString("content")),
                        rs.getObject("version", Long.class)),
                type);
    }

    /** Later time; with the same time, the higher replica id (as the replication does). */
    static boolean isLater(long stamp, String node, long otherStamp, String otherNode) {
        int byTime = Long.compare(stamp, otherStamp);
        return byTime != 0 ? byTime > 0 : (node == null ? "" : node).compareTo(otherNode == null ? "" : otherNode) > 0;
    }

    /** An entity as kept by the read model: its view and its version (the one its ETag is built from). */
    public record Row(String key, JsonNode view, Long version) {
    }
}
