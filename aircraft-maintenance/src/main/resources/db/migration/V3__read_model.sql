-- Read model (CQRS): every aircraft, aircraft model, maintenance record and flight copy of every
-- replica, with the view the queries need (JSON), kept up to date from the events on RabbitMQ.
-- Only used when aisafe.events.enabled=true. A deleted entity stays as a row marked deleted, and
-- every row keeps the stamp of its last change (as replica_state does).
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
