-- Read model of the routes (CQRS): every route of every replica, kept up to date from the route
-- events on RabbitMQ. Only used when aisafe.events.enabled=true. A deleted route stays as a row
-- marked deleted, and every row keeps the stamp of its last change (as replica_state does).
CREATE TABLE route_view (
    origin_iata_code varchar(3) not null,
    destination_iata_code varchar(3) not null,
    estimated_flight_time integer,
    minimum_range float(53),
    minimum_capacity integer,
    status varchar(20),
    version bigint,
    stamp bigint not null,
    stamp_node varchar(255) not null,
    deleted boolean not null,
    primary key (origin_iata_code, destination_iata_code)
);
