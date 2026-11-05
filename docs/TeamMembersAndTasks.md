# SIDIS - Team and tasks

## 1. Team members

| Student number | Name           |
| -------------- | -------------- |
| **1241692**    | Diogo Nogueira |
| **1231562**    | Vasco Magolo   |
| **1241131**    | Miguel Silva   |
| **1241034**    | Rui Marinheiro |

## 2. Ownership

Each service has an owner, who answers for its code and its documentation; the shared pieces (the `common`
library, the cluster, the tests that cross services) are a team responsibility. As in PSOFT, every member
must understand the whole system, not only the part they own.

| Part                                                                               | Owner          | Monolith work packages                             |
| ---------------------------------------------------------------------------------- | -------------- | -------------------------------------------------- |
| aircraft-maintenance: aircraft and aircraft models                                 | Diogo Nogueira | WP1A, WP1B, US223                                  |
| aircraft-maintenance: maintenance                                                  | Rui Marinheiro | WP4A, WP4B                                         |
| airports-routes                                                                    | Miguel Silva   | WP2A, WP2B, WP3A, WP3B (US214-US216), US227, US228 |
| flight-ops (with the scheduling Saga)                                              | Vasco Magolo   | WP3B (US212, US213), US229                         |
| login, `common` (forwarding, replication, security, audit), cluster and deployment | Diogo Nogueira | -                                                  |

### Assignment 2

| Part                                                                                                                          | Owner          |
| ----------------------------------------------------------------------------------------------------------------------------- | -------------- |
| airports-routes: events and read model (CQRS)                                                                                 | Miguel Silva   |
| `common` for every service (event outbox, read models, commands, snapshots), aircraft-maintenance (CQRS, C4, unicorns), login | Diogo Nogueira |
| airports-routes and flight-ops reading the fleet from events, the Saga's C4 and C5 over the broker (reviewed by their owners) | Diogo Nogueira |
| RabbitMQ in k3s, Postman of Assignment 2, [`assignment-2.md`](assignment-2.md)                                                | Diogo Nogueira |

## 3. What each pull request brought

| PR  | Author         | Merged     | Content                                                                                           |
| --- | -------------- | ---------- | ------------------------------------------------------------------------------------------------- |
| #1  | Diogo Nogueira | 2026-10-18 | aircraft-maintenance and login extracted, with replication (`common`) and the cluster (`deploy/`) |
| #2  | Miguel Silva   | 2026-10-18 | airports-routes extracted (C1, C3, US203 and US227 moved here)                                    |
| #3  | Diogo Nogueira | 2026-10-18 | 4 GB nodes, restart of changed VMs, failure test cleanup                                          |
| #4  | Diogo Nogueira | 2026-10-18 | database structure with Flyway                                                                    |
| #5  | Diogo Nogueira | 2026-10-21 | distributed Postman collection                                                                    |
| #6  | Miguel Silva   | 2026-10-21 | airports-routes documentation and `docs_monolith`                                                 |
| #7  | Miguel Silva   | 2026-10-21 | airports-routes in the Postman collection                                                         |
| #8  | Vasco Magolo   | 2026-10-22 | flight-ops extracted, with the scheduling Saga (C2, C4, C5)                                       |
| #9  | Miguel Silva   | 2026-10-25 | documentation of the whole system and of every service                                            |
| #10 | Diogo Nogueira | 2026-10-26 | flight ids unique across replicas, copies with the service token                                  |
| #11 | Diogo Nogueira | 2026-10-26 | flight-ops in the Postman collection                                                              |
| #12 | Diogo Nogueira | 2026-10-26 | live demo dashboard                                                                               |
| #13 | Diogo Nogueira | 2026-10-26 | HTTPS on the virtual IPs                                                                          |
| #14 | Diogo Nogueira | 2026-10-30 | audit trail                                                                                       |
| #15 | Diogo Nogueira | 2026-10-30 | parts import restricted to technicians and supervisors (US225)                                    |
| #16 | Diogo Nogueira | 2026-10-30 | use cases with SSD and SD, contracts C1-C5, global artifacts, screenshots of the running system   |
