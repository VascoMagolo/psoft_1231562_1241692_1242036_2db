package aisafe.airportsroutes.infrastructure.readmodel;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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
     * The stamp rule is inside the {@code UPDATE} itself, so two writers of the same row (an event
     * and the reconciliation, e.g. right after a restart) cannot both pass it: the later change
     * always stays.
     *
     * @param view the entity after the change, null if the change deleted it
     * @return whether the change was applied (false: this row already shows a later change)
     */
    public boolean apply(String type, String key, JsonNode view, Long version, long stamp, String stampNode) {
        String node = stampNode == null ? "" : stampNode;
        String content = view == null ? null : objectMapper.writeValueAsString(view);
        if (updateIfLater(type, key, content, version, stamp, node)) {
            return true;
        }
        try {
            jdbc.update("INSERT INTO read_model (content, version, stamp, stamp_node, deleted, entity_type, entity_key) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)", content, version, stamp, node, content == null, type, key);
            return true;
        } catch (DuplicateKeyException e) {
            // The row exists (with a later change), or another writer inserted it first: apply this one only if later
            return updateIfLater(type, key, content, version, stamp, node);
        }
    }

    /** Later time; with the same time, the higher replica id (as the replication does). */
    private boolean updateIfLater(String type, String key, String content, Long version, long stamp, String node) {
        return jdbc.update("UPDATE read_model SET content = ?, version = ?, stamp = ?, stamp_node = ?, deleted = ? "
                        + "WHERE entity_type = ? AND entity_key = ? AND (stamp < ? OR (stamp = ? AND stamp_node < ?))",
                content, version, stamp, node, content == null, type, key, stamp, stamp, node) == 1;
    }

    /** Every entity of this type that is not deleted, ordered by key. */
    public List<Row> findAll(String type) {
        return jdbc.query("SELECT entity_key, content, version FROM read_model WHERE entity_type = ? AND deleted = FALSE "
                        + "ORDER BY entity_key",
                (rs, n) -> new Row(rs.getString("entity_key"), objectMapper.readTree(rs.getString("content")),
                        rs.getObject("version", Long.class)),
                type);
    }

    /** An entity as kept by the read model: its view and its version (the one its ETag is built from). */
    public record Row(String key, JsonNode view, Long version) {
    }
}
