# airports-routes

The Airports & Routes service of AISafe: registration and querying of airports (runways,
facilities, certifications) and the definition and history of flight routes. It runs as three
replicas, each with its own database, that answer for each other.

|                      |                                                                                                               |
| -------------------- | ------------------------------------------------------------------------------------------------------------- |
| Code                 | [`airports-routes/`](../../airports-routes) (packages `airports`, `routes`, `flightcopies`, `fleet`)          |
| Port                 | 8082 (each replica)                                                                                           |
| Entry                | virtual IP of the service (`VIP_AIRPORTS_ROUTES` in `deploy/config.sh`), through the Envoy entry of each node |
| One replica directly | node ports 30820, 30821, 30822 (`airports-routes-0..2`), on any node                                          |
| API documentation    | `/swagger-ui.html` and `/v3/api-docs` of any replica                                                          |
| Depends on           | aircraft-maintenance (C1, C3)                                                                                 |
| Used by              | flight-ops (C2, C5)                                                                                           |

## 1. Logical view

![Logical view](svg/logical-view.svg)

The three instances are equal: the client reaches any of them through the entry, and each one
forwards to its peers what it cannot answer. The only other service it calls is
aircraft-maintenance, and only flight-ops calls it. Dependencies go one way:
flight-ops → airports-routes → aircraft-maintenance.

## 2. Components (C4 level 3)

![Components](svg/components.svg)

Each replica keeps the three layers of the monolith (`domain`, `application`, `infrastructure`),
checked by `ArchitectureTest`. What the distribution added:

| Component                                                                            | Where                          | What it does                                                                         |
| ------------------------------------------------------------------------------------ | ------------------------------ | ------------------------------------------------------------------------------------ |
| `Fleet` / `HttpFleet`                                                                | `fleet`                        | Port to aircraft-maintenance (C1, C3). Sends the user's token and the correlation id |
| `RoutesAcrossReplicas`, `FlightCopiesAcrossReplicas`                                 | `routes`, `flightcopies`       | Every route or flight copy of all replicas, for listings, reports and checks         |
| `RecordFlightCopyUseCase`, `FlightCopyController`                                    | `flightcopies`                 | Receives the copies of the flights from flight-ops (C5)                              |
| `RouteInternalController`, `ViewRouteForServicesUseCase`                             | `routes`                       | Route data for other services and for the peers (C2)                                 |
| `*ReplicaHandler`, `RouteReplicaSender`                                              | `*/infrastructure/replication` | What is copied to other replicas, and how a received copy is applied                 |
| `PeerForwardingAdvice`, `PeerClient`, `ReplicaGatherer`, `Replicator`, `ReplicaSync` | `common`                       | Forwarding and replication shared by every service                                   |

## 3. Domain model

![Domain model](svg/domain-model.svg)

The service keeps its own copies of types that belong to other services (`IataCode` is its own;
`ModelName` names a model of aircraft-maintenance), instead of sharing them through `common`
(decision D1). Aircraft are never stored here: they are asked to aircraft-maintenance every time
(`AircraftInfo`), so the answer is always the owner's.

## 4. Where the data lives

| Data                              | Kept by                                                                 | Why                                                                                                       |
| --------------------------------- | ----------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| Airports and their certifications | **every replica** (catalog)                                             | Few, rarely change, and every write of a route needs both airports (status, coordinates for the distance) |
| Routes (with their history)       | the replica that created it (**home**) and the **next one** in the ring | Partitioned data, as the assignment asks: a replica may not have a route and must ask its peers           |
| Flight copies                     | the replica that received it (home) and the next one                    | Same as routes                                                                                            |

Ring: `airports-routes-0 → 1 → 2 → 0`. With three replicas, a route is on two of them: any one
replica can be lost without losing data.

## 5. Contracts with other services

| #   | Direction              | Request                                       | Answer                                                                                                    |
| --- | ---------------------- | --------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| C1  | → aircraft-maintenance | `GET /internal/aircrafts/{reg}`               | registration, model, status, seat capacity, range, fuel consumption; 404 if it does not exist             |
| C2  | flight-ops →           | `GET /internal/routes/{origin}/{destination}` | status, estimated flight time, minimum capacity and range, **distance in km**; 404 if no replica holds it |
| C3  | → aircraft-maintenance | `GET /internal/aircraft-models/{name}`        | 200 if the model exists, 404 if not                                                                       |
| C5  | flight-ops →           | `PUT /internal/flights/{id}`                  | 200 stored (idempotent); 404 route does not exist; 409 route not active                                   |

All of them are commands or queries over HTTP, with the user's token (the C5 copies that flight-ops
sends outside a user's request carry its service token instead): there are no events or
message queues (Assignment 1). Calls to aircraft-maintenance go to the Envoy entry of the same
node, which picks a ready replica and repeats the request on another one if it fails. Timeouts:
1 s to connect, 10 s to answer.

**Routes that changed service (D2):** US203 and US227 used `/api/aircrafts/...` in the monolith. They
need the routes, and aircraft-maintenance must not depend on airports-routes, so they moved here:

| US                      | Monolith                                     | Now                                                                     |
| ----------------------- | -------------------------------------------- | ----------------------------------------------------------------------- |
| US203 compatible routes | `GET /api/aircrafts/{reg}/compatible-routes` | `GET /api/routes/compatible?aircraft={reg}`                             |
| US227 fuel efficiency   | `GET /api/aircrafts/{reg}/fuel-efficiency`   | `GET /api/routes/{origin}/{destination}/fuel-efficiency?aircraft={reg}` |

## 6. Scenarios (C4+1)

### 6.1 GET forwarded to the peers, with a replica down

![Forwarding](svg/sd_forwarding_get_route.svg)

| Situation                                                    | Answer                                                                                                           |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| Found locally                                                | 200                                                                                                              |
| Found on a peer                                              | 200, with `X-Data-Node` naming the peer                                                                          |
| No replica has it, all answered                              | 404                                                                                                              |
| No replica that answered has it, some did not answer         | **503**, `Retry-After: 1`, `X-Partial-Result: true`, `X-Unreachable-Peers` (never a 404 for data that may exist) |
| Request already forwarded (`X-Peer-Forwarded`)               | never forwarded again: one hop, no loops                                                                         |
| `PUT`/`PATCH`/`DELETE` of a route this replica does not hold | sent to the peers one at a time until one holds it                                                               |
| `POST`                                                       | never forwarded: the new route stays on the replica that created it                                              |

Robustness (`PeerClient`): peers asked in parallel; 0.5 s to connect, 2 s to answer; 1 retry with
backoff; circuit breaker per peer (3 failures → skipped for 10 s, closed as soon as that peer
contacts this replica again). Every attempt is logged with the replica and the correlation id.

These cases are tested end to end by the [Postman collection](../postman/README.md) (folders 1, 2,
3, 4, 7 and 8 for airports-routes).

### 6.2 US203 compatible routes: another service and every replica

![US203](svg/sd_us203_compatible_routes.svg)

### 6.3 C5 copy of a flight: a step of the scheduling Saga

![C5](svg/sd_c5_flight_copy.svg)

When flight-ops schedules a flight, sending the copy here confirms that the route is still active.
A 404 or 409 makes flight-ops undo the steps already done (the aircraft reservation in
aircraft-maintenance, the flight itself). The Saga is described with flight-ops.

## 7. Listings and reports over every replica

Routes are partitioned, so any use case that needs **all** routes or flight copies reads them
from every replica (`ReplicaGatherer`): it takes the local ones and those of each peer
(`GET /internal/replicas/{type}`), keeps one entry per key (the newest version) and only then
sorts and pages. Summing partial results of each replica would count each route twice (it is
on two replicas).

| Use case                                                 | Reads every replica                                               |
| -------------------------------------------------------- | ----------------------------------------------------------------- |
| US113 routes from an airport, US209 routes of an airport | routes                                                            |
| US114 search routes                                      | routes                                                            |
| US203 compatible routes                                  | routes (+ C1)                                                     |
| US214 active routes, by distance or popularity           | routes + flight copies (popularity = number of flights per route) |
| US215 total network distance, US216 alternative routes   | routes                                                            |
| US210 busiest airports                                   | routes                                                            |
| US228 export network                                     | routes                                                            |
| Delete airport (in use by a route?)                      | routes                                                            |

`X-Unreachable-Peers` lists the replicas that did not answer; `X-Partial-Result: true` is added
only when data may be missing (both holders of some routes did not answer).

## 8. Consistency

- **AP (availability and partition tolerance), eventual consistency.** A write is answered by
  one replica and copied to the others right after: to every other replica for airports and
  certifications, to the successor for routes and flight copies. A copy that fails stays in an
  outbox and is retried; every 30 s each replica also asks its peers for what it should hold
  (`ReplicaSync`), which repairs anything missed.
- **Conflicts:** the newest change wins (time of the change plus replica id to break ties).
  Deletes leave a tombstone, so an old copy does not bring a deleted route back.
- **Unique routes:** creating a route checks the local database and the peers (C2 on them).
  This is best effort: during a network partition a duplicate can be created. Flyway migration
  `V2__one_route_per_origin_and_destination.sql` keeps one row per origin and destination in each
  database.
- **Optimistic concurrency:** `If-Match` / `ETag` with the version, as in the monolith. The copy
  keeps the version of the replica that sent it, so the ETag is the same on both holders.

## 9. Failures

| Failure                              | Behaviour                                                                                            |
| ------------------------------------ | ---------------------------------------------------------------------------------------------------- |
| One replica down                     | Its routes are still served by the successor; listings complete; the entry stops sending it requests |
| Replica restarts (disk kept)         | Asks its peers for what changed before reporting ready                                               |
| Replica reborn empty on another node | Recovers the catalogs and its routes from the peers before reporting ready                           |
| aircraft-maintenance down            | US106a, US203 and US227 answer **503** with `Retry-After`; everything else keeps working             |
| flight-ops down                      | Nothing here depends on it; flight copies already received keep counting in US214                    |

## 10. Security

- Every request needs a JWT issued by login; this service only validates it, without calling login.
- Access by role (`SecurityConfig`), the same rules as the monolith for airports and routes; US203
  and US227 require ATCC.
- `/internal/replicas/**` (replication between replicas): role SERVICE only, the token of a replica.
- `/internal/**` (C2, C5): authenticated with the user's token forwarded by the calling service
  (or flight-ops' service token for C5 copies outside a user's request); not reachable through the
  virtual IP (404 at the entry).
- Every log line carries the replica (`nodeId`) and the correlation id (`X-Correlation-Id`), also
  across services.

## 11. Known limitations

- **Unique routes during a partition:** a duplicate can be created (section 8).
- **Last write wins:** of two changes to the same route at the same time on different replicas,
  one is lost without warning; it depends on the clocks of the nodes being synchronised (NTP).
- **Two replicas lost at once with their disks:** the routes only they held are lost (two copies
  survive one failure, not two).
- **Right after a replica restarts:** for a few seconds a request may get 503 (try again) for a
  route that exists.
- **Tombstones are never removed:** irrelevant with the volumes of the project.
- **No TLS yet** between services and replicas (plain HTTP inside the cluster).

## 12. Assignment 2: events and CQRS

Off by default (`EVENTS_ENABLED=false`): without it the service works exactly as described above,
with no broker. With `EVENTS_ENABLED=true` it needs RabbitMQ (`RABBITMQ_HOST`, `RABBITMQ_PORT`,
`RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`).

![CQRS](svg/sd_cqrs_read_model.svg)

### 12.1 Events it publishes

Topic exchange `airports-routes.events` (durable). Published by the replica where the change was
made; a copy received from a peer is not published again (`EventPublisher`, in `common`). The event
is written to the outbox in the transaction of the change and sent once committed, removed only when
the broker confirms it (`EventRelay`): with the broker down nothing is lost
([`../assignment-2.md`](../assignment-2.md), section 6).

| Routing key                                              | When                              | `data`                                                                                                                                                            |
| -------------------------------------------------------- | --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `route.created`, `route.updated`                         | a route is saved                  | the route (`RouteView`): origin, destination, flight time, minimum range and capacity, status, distance in km (flight-ops checks it against the aircraft's range) |
| `airport.created`, `airport.updated`                     | an airport is saved               | the airport as the clients get it (`AirportResponse`: photos only counted)                                                                                        |
| `flightcopy.created`, `flightcopy.updated`               | a copy of a flight is stored (C5) | the copy: route, aircraft, times, status, version                                                                                                                 |
| `route.deleted`, `airport.deleted`, `flightcopy.deleted` | deleted                           | none                                                                                                                                                              |

Body, as JSON: `type` (the routing key), `key` (`OPO-LIS`, the IATA code or the flight id), `status`,
`version` (the one the ETag is built from), `data`, `stamp` and `stampNode` (the time and replica of
the change, the same stamp the replication gives it), `nodeId` (the replica that published it),
`occurredAt`.

### 12.2 Read model (CQRS)

| Side                                                                                                       | Where                                                                                                                            | Kept by                               |
| ---------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| Write (commands: `POST`, `PUT`, `PATCH`, `DELETE`; and the details of one entity, read before changing it) | the tables of each entity: routes and flight copies partitioned (home and next replica), airports on every replica (section 4)   | the use cases, as in Assignment 1     |
| Read (every listing and report: section 7, airport search and grouping, US210, US214)                      | table `read_model` of each replica: **every** route, airport and flight copy of **every** replica, as the queries need it (JSON) | the events only (`ReadModelListener`) |

| Query port                                                            | Without events (Assignment 1)                   | With events                                                             |
| --------------------------------------------------------------------- | ----------------------------------------------- | ----------------------------------------------------------------------- |
| `RoutesAcrossReplicas` (route listings, reports)                      | `GatheredRoutes`: asks the peers                | `ReadModelRoutes`                                                       |
| `FlightCopiesAcrossReplicas` (route popularity, US214)                | `GatheredFlightCopies`: asks the peers          | `ReadModelFlightCopies`                                                 |
| `AirportListings` (search, grouping by region or country, statistics) | `LocalAirportListings`: this replica's airports | `ReadModelAirportListings`: the stored answers, no domain objects built |

- Each replica has its own quorum queue, `airports-routes.read-model.<replica>`, bound with
  `route.*`, `airport.*` and `flightcopy.*` (publish/subscribe): every replica gets every event,
  also those of its own changes.
- The use cases do not change (they read the ports), and **a listing no longer depends on the other
  replicas being up** (no `X-Partial-Result`).
- What the read model keeps of each entity is a `Projection` (`RouteProjection`, `AirportProjection`,
  `FlightCopyProjection`): the routing keys, the view, and how to build it from what the replicas
  export.
- The details of one entity (`GET /api/routes/{o}/{d}`, `GET /api/airports/{iata}`) still read the
  write model: they are read to change the entity, with the current `version` for `If-Match`, so
  they must never be behind.
- Order: each row keeps the stamp of the change it shows, and a change is applied only if it is
  later (same rule as the replication). Events can arrive in any order, and twice. A deleted entity
  stays as a row marked deleted, so an older event cannot bring it back. The rule is inside the
  `UPDATE` itself (`... WHERE stamp < ? OR (stamp = ? AND stamp_node < ?)`), so an event and the
  reconciliation writing the same row at once (e.g. right after a restart) always leave the later
  change.
- Lost events: `ReadModelReconciler` compares the read model with what the replicas hold (the same
  export the replication uses): this replica's own, and for routes and flight copies also the
  peers' (`GET /internal/replicas/{type}`); airports are a catalog, already all on this replica. It
  runs on startup, before the replica reports ready, and then every `READ_MODEL_RECONCILE_MS` (60 s
  by default). A change it cannot apply is logged and skipped, so it never stops the startup.
- The read model is a table of its own in the replica's database, written only from events: the two
  models are independent (different tables, different writers, different shape). A second database
  only for reading would also separate their storage, at the cost of one more database per replica;
  with H2 per replica it adds nothing the separate table does not give.
- An event that cannot be applied is not requeued forever: after 5 deliveries it goes to the
  dead-letter queue `airports-routes.dead-letter`; the reconciliation repairs that row.

### 12.2b Reading the fleet, and C5, through the broker

- **The fleet** (instead of C1 and C3): each replica keeps the aircraft and aircraft models of
  aircraft-maintenance in its read model, from their events (`aircraft.*`, `aircraftmodel.*`) and a
  snapshot asked on startup (`ReadModelFleet`, `FleetProjections`). US203 and US227 read this copy;
  an aircraft not in it yet is asked to aircraft-maintenance over the broker (`aircraft.get`).
- **C5** arrives as the command `flightcopy.record` (`FlightCopyCommandHandler`), handled by the same
  use case as `PUT /internal/flights/{id}`, with the same answers.
- **C2** for flight-ops: the `route.*` events, and the query `route.get` when flight-ops does not
  have a route yet (`RouteQueryHandler`, from the write model, with the peers).

### 12.3 Consistency and failures

| Situation                  | Behaviour                                                                                                                                                                  |
| -------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Normal                     | A change shows in the listings of every replica a few milliseconds later (eventual consistency, also on the replica that made the change)                                  |
| Two replicas down          | The third still lists every route, airport and the popularity of every route (its read model has them all); writes to routes only those two held answer as in Assignment 1 |
| RabbitMQ down              | Writes still work; their events wait in the outbox and leave, in order, when the broker is back. Listings show the data as it was meanwhile                                |
| A replica down for a while | Its queue keeps the events (durable); it applies them when it comes back, and reconciles on startup                                                                        |

Tested end to end by the [events collection](../postman/README.md#assignment-2-airports-routes-events-locally)
(44 requests, 102 checks), and live with 3 replicas: listings complete with two replicas down, and
the changes made while RabbitMQ was down reached every replica within one reconciliation.

The rest of Assignment 2 (the other services, RabbitMQ in k3s, the Saga over the broker,
`/api/v2/unicorns`) is in [`../assignment-2.md`](../assignment-2.md).
