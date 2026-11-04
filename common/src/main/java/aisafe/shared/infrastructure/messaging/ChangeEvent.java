package aisafe.shared.infrastructure.messaging;

import java.time.Instant;

/**
 * Body of every event, as JSON: an entity created, updated or deleted.
 *
 * @param type       the routing key, {@code <entity>.<change>} (e.g. {@code route.created})
 * @param key        the entity's key ({@code OPO-LIS}, a registration, a flight id...)
 * @param status     its status after the change, null when deleted
 * @param version    its version after the change (the one its ETag is built from), if any
 * @param data       the entity after the change (its view for the read models), null when deleted
 * @param stamp      time of the change, the same stamp the replication gives it
 * @param stampNode  replica that stamped it (ties are broken by this id)
 * @param nodeId     replica that published the event
 * @param occurredAt when the event was written
 */
public record ChangeEvent(String type, String key, String status, Long version, Object data,
                          long stamp, String stampNode, String nodeId, Instant occurredAt) {
}
