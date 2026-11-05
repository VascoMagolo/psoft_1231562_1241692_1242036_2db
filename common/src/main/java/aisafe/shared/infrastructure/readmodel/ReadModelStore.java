package aisafe.shared.infrastructure.readmodel;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

/**
 * The read model (CQRS): table {@code read_model}, one row per entity, with the view the queries
 * need as JSON. It holds every entity of every replica of this service (kept by its own events and
 * the {@link ReadModelReconciler}), and the entities of other services this service reads (kept by
 * their events and the {@link SnapshotClient}). It is written only from there; the write model (the
 * tables of each entity) stays as in Assignment 1, partitioned between the replicas.
 *
 * <p>Each row keeps the stamp of the change it shows. A change is applied only if it is later than
 * that one, the same rule ("last writer wins") as the replication, so events, reconciliation and
 * snapshots can arrive in any order, and twice. A deleted entity stays as a row marked deleted, so
 * an older event cannot bring it back.</p>
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
     * The stamp rule is inside the {@code UPDATE} itself, so two writers of the same row (an event
     * and the reconciliation, e.g. right after a restart) cannot both pass it: the later change
     * always stays.
     *
     * @param view the entity after the change, null if the change deleted it
     * @return whether the change was applied (false: this row already shows this change or a later one)
     */
    public boolean apply(String type, String key, JsonNode view, Long version, long stamp, String stampNode) {
        String node = stampNode == null ? "" : stampNode;
        // A deletion read from JSON may come as a JSON null rather than no view at all
        String content = view == null || view.isNull() ? null : objectMapper.writeValueAsString(view);
        if (updateIfLater(type, key, content, version, stamp, node)) {
            return true;
        }
        if (exists(type, key)) {
            // Not later than the row, or another writer inserted it since: decided by the UPDATE again
            return updateIfLater(type, key, content, version, stamp, node);
        }
        try {
            jdbc.update("INSERT INTO read_model (content, version, stamp, stamp_node, deleted, entity_type, entity_key) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)", content, version, stamp, node, content == null, type, key);
            return true;
        } catch (DuplicateKeyException e) {
            // Another writer inserted it in the meantime: apply this one only if later
            return updateIfLater(type, key, content, version, stamp, node);
        }
    }

    /** Later time; with the same time, the higher replica id (as the replication does). */
    private boolean updateIfLater(String type, String key, String content, Long version, long stamp, String node) {
        return jdbc.update("UPDATE read_model SET content = ?, version = ?, stamp = ?, stamp_node = ?, deleted = ? "
                        + "WHERE entity_type = ? AND entity_key = ? AND (stamp < ? OR (stamp = ? AND stamp_node < ?))",
                content, version, stamp, node, content == null, type, key, stamp, stamp, node) == 1;
    }

    private boolean exists(String type, String key) {
        Integer rows = jdbc.queryForObject("SELECT COUNT(*) FROM read_model WHERE entity_type = ? AND entity_key = ?",
                Integer.class, type, key);
        return rows != null && rows > 0;
    }

    /** Every entity of this type that is not deleted, ordered by key. */
    public List<Row> findAll(String type) {
        return jdbc.query("SELECT entity_key, content, version FROM read_model WHERE entity_type = ? AND deleted = FALSE "
                        + "ORDER BY entity_key",
                (rs, n) -> new Row(rs.getString("entity_key"), objectMapper.readTree(rs.getString("content")),
                        rs.getObject("version", Long.class)),
                type);
    }

    /** One entity, unless the read model does not have it or it is deleted. */
    public Optional<Row> find(String type, String key) {
        return jdbc.query("SELECT entity_key, content, version FROM read_model "
                                + "WHERE entity_type = ? AND entity_key = ? AND deleted = FALSE",
                        (rs, n) -> new Row(rs.getString("entity_key"), objectMapper.readTree(rs.getString("content")),
                                rs.getObject("version", Long.class)),
                        type, key)
                .stream().findFirst();
    }

    /** Every row of this type, deleted ones included, with the stamp of its change: what a snapshot sends. */
    public List<StampedRow> findAllStamped(String type) {
        return jdbc.query("SELECT entity_key, content, version, stamp, stamp_node, deleted FROM read_model "
                        + "WHERE entity_type = ? ORDER BY entity_key",
                (rs, n) -> {
                    String content = rs.getString("content");
                    return new StampedRow(rs.getString("entity_key"),
                            content == null ? null : objectMapper.readTree(content),
                            rs.getObject("version", Long.class), rs.getLong("stamp"), rs.getString("stamp_node"));
                },
                type);
    }

    /** An entity as kept by the read model: its view and its version (the one its ETag is built from). */
    public record Row(String key, JsonNode view, Long version) {
    }

    /** A row with the stamp of the change it shows; {@code view} is null when the entity was deleted. */
    public record StampedRow(String key, JsonNode view, Long version, long stamp, String stampNode) {
    }
}
