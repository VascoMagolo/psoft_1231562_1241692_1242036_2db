# US113 - View Routes

## User Story

> As an **ATCC**, I want to view all routes from a specific airport, and to view the details of a route given its ID.

---

## Acceptance Criteria

- The system must allow listing routes departing from a specific origin airport.
- The system must allow retrieving route details by route ID.
- If the airport or route does not exist, the system returns HTTP 404.
- On success the system returns HTTP 200 with the requested data.

---

## Pre-conditions

- The actor is authenticated as an ATCC.

---

## Post-conditions

- Route information is retrieved and presented to the actor.

---

## Main Success Scenario

### View Routes by Airport

1. The ATCC requests `GET /api/routes/airport/{iataCode}`.
2. The system validates the airport existence.
3. The system retrieves all matching routes.
4. The system returns HTTP 200 with the route list.

### View Route Details

1. The ATCC requests `GET /api/routes/{origin}/{destination}`.
2. The system validates the route existence.
3. The system retrieves the route details.
4. The system returns HTTP 200 with the route information.

---

## Alternative / Exception Flows

| Step | Condition         | System Response          |
| ---- | ----------------- | ------------------------ |
| 2    | Airport not found | HTTP 404                 |
| 2    | Route not found   | HTTP 404                 |
| 3    | No routes found   | HTTP 200 with empty list |

---

## Design Justification

- Route retrieval operations are separated from modification operations following CQRS-inspired principles.
- Query operations are optimized for filtering by airport and retrieving by route ID.

## Implementation Note

A route has no surrogate numeric ID in the domain model. Its natural key is the **origin + destination IATA code pair**, which is unique by domain rule. The "ID" referred to in the user story is fulfilled by this pair: route details are accessed via `GET /api/routes/{origin}/{destination}` (e.g., `GET /api/routes/OPO/LIS`).

---

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us113.svg)
- [Sequence Diagram](svg/sd_us113.svg)

---
