# Postman: distributed tests

`aisafe-distributed.postman_collection.json` tests the distributed AISafe end to end, against the
cluster built by `deploy/setup.sh`: 86 requests, 199 checks. The collection of the monolith is in
[`docs_monolith/postman/`](../../docs_monolith/postman/psoft.postman_collection.json), kept for reference.

| Folder                          | What it shows                                                                                                                                                                                                                                                                                                                                                                  |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 0. Setup                        | One login per role (through the login's virtual IP), and the 3 replicas of each service ready                                                                                                                                                                                                                                                                                  |
| 1. Local access                 | Data held by the replica asked is answered by it, without asking its peers                                                                                                                                                                                                                                                                                                     |
| 2. Forwarding between replicas  | An aircraft and a route created on replica 0, read on replica 1 (holds the copy) and on replica 2 (forwards: `X-Data-Node` names the replica that held it); an unknown aircraft or route gives 404 after asking every peer                                                                                                                                                     |
| 3. Aggregation                  | The list holds the records of every replica; lists and reports (including route popularity, US214, which counts the flight copies of every replica) give the same data on the 3 replicas                                                                                                                                                                                       |
| 4. Catalogs                     | An aircraft model created on replica 2, and an airport created on replica 1, are held by the other replicas too                                                                                                                                                                                                                                                                |
| 5. Between services             | airports-routes asks aircraft-maintenance whether a model (C3) or an aircraft (C1) exists, and for the fuel consumption of an aircraft (US227, C1)                                                                                                                                                                                                                             |
| 6. Flights: the scheduling Saga | One flight scheduled on each flight-ops replica (the Saga: route C2, aircraft C1, overlaps on every replica, copies C4 and C5); every replica lists all three; an overlapping flight held by another replica gives 409, an unknown aircraft or route 404; aircraft-maintenance (the aircraft cannot be deleted) and airports-routes (the route's flight count) hold the copies |
| 7. Access control               | A role that may not do something gets 403; so does a request without a token; through the virtual IPs, `/internal/**` and the database console answer 404                                                                                                                                                                                                                      |
| 8. Failures                     | With a replica of each service down, the same data is still answered by the others                                                                                                                                                                                                                                                                                             |
| 9. Cleanup                      | Deletes what the run created (through replicas that forward the deletion), so it can run again. The aircraft of folder 6 stays: flights cannot be canceled through the API, and an aircraft with flights cannot be deleted                                                                                                                                                     |

Every response says which replica answered (`X-Node-Id`), and, when forwarded, which replica held
the data (`X-Data-Node`). Peers that did not answer are listed in `X-Unreachable-Peers`. The run
prints these headers in the console.

## Running it

The virtual IPs answer only with HTTPS, with a self-signed certificate (`deploy/entry.crt`, saved by
`deploy/setup.sh`); the node ports of single replicas are plain HTTP.

**Postman:** import both files, select the environment "AISafe k3s", and run the collection (or
the folders in order). With a cluster other than the reference one, set `node` (any node's IP) and
the virtual IPs in the environment to the values of `deploy/config.sh`. To trust the certificate:
Settings → Certificates → CA certificates, select `deploy/entry.crt`.

**Command line** ([newman](https://www.npmjs.com/package/newman)):

```sh
newman run docs/postman/aisafe-distributed.postman_collection.json \
  -e docs/postman/aisafe-k3s.postman_environment.json --ssl-extra-ca-certs deploy/entry.crt
```

**Folder 8 with a replica really down:** run folders 0, 2 and 6, stop a replica, then run folder 8
(and folder 9 once it is back). In Postman, the environment keeps the values between runs; with
newman, pass them on with `--export-environment`:

```sh
newman run docs/postman/aisafe-distributed.postman_collection.json -e docs/postman/aisafe-k3s.postman_environment.json \
  --ssl-extra-ca-certs deploy/entry.crt --folder "0. Setup: logins and every replica ready" --folder "2. Forwarding between replicas (peer-to-peer)" \
  --folder "6. Flights: the scheduling Saga (flight-ops)" --export-environment /tmp/aisafe-env.json
kubectl -n aisafe delete pod aircraft-maintenance-1 airports-routes-1 flight-ops-1 --grace-period=0 --force
newman run docs/postman/aisafe-distributed.postman_collection.json -e /tmp/aisafe-env.json \
  --ssl-extra-ca-certs deploy/entry.crt --folder "8. Failures: with a replica down"
```

## Assignment 2: messaging and CQRS on the cluster

`aisafe-a2.postman_collection.json` tests Assignment 2 against the cluster (environment "AISafe k3s",
which also has `rabbit_vip`): 33 requests, 84 checks. It reads the queues through the RabbitMQ
management API (HTTPS on `rabbit_vip`, the same certificate) as the user `postman`, which may only
create queues named `postman.*` and bind them to the events of the services.

| Folder                                  | What it shows                                                                                                                                                                                                                                                                                                                       |
| --------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0. Setup                                | Logins; the management API as `postman`; a test queue `postman.events` bound to `aircraft.*` (aircraft-maintenance) and `flightcopy.*` (airports-routes), playing another service                                                                                                                                                   |
| 1. The broker                           | The read-model queues of three replicas and the command queues of aircraft-maintenance (C4) and airports-routes (C5): quorum queues, with a dead-letter exchange, taken by their replicas                                                                                                                                           |
| 2. Command, then event, then read model | An aircraft created on aircraft-maintenance-1 publishes `aircraft.created` (with the aircraft and its stamp); aircraft-maintenance-0 does not hold it (its read of it is forwarded: `X-Data-Node`), but the unicorns of all three replicas list it, from their read models                                                          |
| 3. Between services over AMQP           | airports-routes finds the routes compatible with the new aircraft (US203) from its copy of the fleet, without C1; flight-ops schedules a flight with it (the Saga, with C4 and C5 as commands through the broker); airports-routes publishes `flightcopy.created` and counts the flight; the aircraft cannot be deleted (C4 stored) |
| 4. A change travels to every copy       | The aircraft goes under maintenance on aircraft-maintenance-2: `aircraft.updated` arrives, the unicorns of replica 0 turn `GRUMPY`, and flight-ops refuses a new flight with it (409)                                                                                                                                               |
| 5. `/api/v2/unicorns`                   | A list with a mood per aircraft, from the read model, any role; without a token, refused                                                                                                                                                                                                                                            |
| 6. Cleanup                              | Deletes the test queue (the aircraft stays: it has a flight)                                                                                                                                                                                                                                                                        |

The password of `postman` is in the cluster's Secret, never in the repository: give it to newman
(or set `postman_password` in the Postman environment) from a computer with access to a node:

```sh
newman run docs/postman/aisafe-a2.postman_collection.json -e docs/postman/aisafe-k3s.postman_environment.json \
  --ssl-extra-ca-certs deploy/entry.crt --env-var "postman_password=$(ssh sidis@192.168.124.43 \
  "sudo k3s kubectl -n aisafe get secret aisafe-rabbitmq -o go-template='{{index .data \"postman-password\"}}'" | base64 -d)"
```

The Assignment 1 collection runs unchanged against the cluster with events on (86 requests, 199
checks): the external API did not change.

## Assignment 2: airports-routes events, locally

`aisafe-events.postman_collection.json` tests the events airports-routes publishes on RabbitMQ and
its read model (CQRS) of routes, airports and flight copies: 44 requests, 102 checks. It runs against
a local setup with the environment `aisafe-local-events.postman_environment.json` (replicas named
`ar0`, `ar1`, `ar2`).

| Folder                                | What it shows                                                                                                                           |
| ------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| 0. Setup                              | Logins, the 3 replicas ready, the exchange `airports-routes.events` and one read-model queue per replica, each with its consumer        |
| 1. Prepare                            | A test queue bound to the exchange with `route.*`, `airport.*` and `flightcopy.*`, playing another service interested in these events   |
| 2. Command, then event                | Creating a route on replica 1 (HTTP) publishes `route.created`, with the route, its version and the stamp of the change                 |
| 3. Read model (CQRS)                  | The 3 replicas list the new route from their own read model, also replica 0, which holds no copy of it                                  |
| 4. Flight copies and route popularity | A copy of a flight (C5) publishes `flightcopy.created`; the route's popularity (US214) grows by one on the 3 replicas                   |
| 5. Airports                           | Registering an airport publishes `airport.created`; the airport search of replicas 0 and 2 finds it in the read model                   |
| 6. Update                             | The route's details give its version; deactivating it with that `If-Match` publishes `route.updated`; the read models show it inactive  |
| 7. Delete                             | Deleting the route and the airport through replica 0 publishes `route.deleted` and `airport.deleted`; no read model lists them any more |
| 8. Cleanup                            | Deletes the test queue                                                                                                                  |

The queues are read through the RabbitMQ management API (HTTP), since Postman does not speak AMQP.

**Local setup** (from the repository root, after `./mvnw -pl airports-routes,login -am package -DskipTests`):

```sh
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
java -jar login/target/login-0.0.1-SNAPSHOT.jar &
# 3 replicas in a ring; *.localtest.me resolves to 127.0.0.1, and its first label is the replica id
PEERS=http://ar0.localtest.me:18090,http://ar1.localtest.me:18091,http://ar2.localtest.me:18092
for i in 0 1 2; do
  EVENTS_ENABLED=true NODE_ID=ar$i PORT=1809$i PEERS=$PEERS SEED_NODE=ar0 \
    java -jar airports-routes/target/airports-routes-0.0.1-SNAPSHOT.jar &
done
newman run docs/postman/aisafe-events.postman_collection.json \
  -e docs/postman/aisafe-local-events.postman_environment.json
```

The RabbitMQ console is at http://127.0.0.1:15672 (guest/guest): the exchange, the queues and the
messages can be seen there while the collection runs.
