# Contracts between services

**Assignment 2:** with events on (the k3s deployment), these contracts go through RabbitMQ: C1, C2
and C3 become events kept in a local copy (asked to the owner over the broker only on a miss), and C4
and C5 commands with an answer. Same data and same answers; see
[`../../assignment-2.md`](../../assignment-2.md), section 3. The pages below describe them as calls
over HTTP (Assignment 1, and the mode without events).

The services talk only through these five HTTP calls: commands or queries, no events or message queues (Assignment 1). Each goes through the entry (Envoy) of the caller's own machine, which picks a ready replica of the owner, and carries the actor's JWT and the correlation id. Dependencies go one way: flight-ops → airports-routes → aircraft-maintenance. Clients cannot call them: through the virtual IPs, `/internal/**` answers 404 at the entry; the services reach it through the entry's plain HTTP ports, inside the cluster.

| #                  | Caller → owner                                     | Request                                       | Kind                      | Used by                    |
| ------------------ | -------------------------------------------------- | --------------------------------------------- | ------------------------- | -------------------------- |
| [C1](C1/README.md) | airports-routes, flight-ops → aircraft-maintenance | `GET /internal/aircrafts/{registration}`      | query                     | US203, US227, US212, US213 |
| [C2](C2/README.md) | flight-ops → airports-routes                       | `GET /internal/routes/{origin}/{destination}` | query                     | US212                      |
| [C3](C3/README.md) | airports-routes → aircraft-maintenance             | `GET /internal/aircraft-models/{name}`        | query                     | US106a                     |
| [C4](C4/README.md) | flight-ops → aircraft-maintenance                  | `PUT /internal/flights/{id}`                  | command (idempotent copy) | US212, US225               |
| [C5](C5/README.md) | flight-ops → airports-routes                       | `PUT /internal/flights/{id}`                  | command (idempotent copy) | US212, US225               |

The Saga that uses C1, C2, C4 and C5: [US212](../use-cases/flight-ops/US212/README.md) and [the scenario in the system documentation](../../README.md#102-scheduling-a-flight-saga).
