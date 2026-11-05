-- Read model (CQRS): the aircraft and aircraft models of aircraft-maintenance and the routes of
-- airports-routes, kept up to date from their events on RabbitMQ (instead of C1, C2, C3). Only
-- used when aisafe.events.enabled=true. A deleted entity stays as a row marked deleted, and every
-- row keeps the stamp of its last change (as replica_state does).
CREATE TABLE read_model (
    entity_type varchar(30) not null,
    entity_key varchar(500) not null,
    content clob,
    version bigint,
    stamp bigint not null,
    stamp_node varchar(255) not null,
    deleted boolean not null,
    primary key (entity_type, entity_key)
);
