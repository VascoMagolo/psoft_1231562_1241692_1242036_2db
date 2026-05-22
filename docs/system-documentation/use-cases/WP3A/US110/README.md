# US110 - Create a Flight Route

## User Story

> As an **ATCC**, I want to create a flight route by specifying origin airport, destination airport, estimated flight time, and minimum aircraft requirements (range, capacity), so that routes can be planned and managed in the system.

---

## Acceptance Criteria

- Both origin and destination airports must exist in the system.
- Origin and destination airports cannot be the same.
- Estimated flight time is mandatory and must be positive.
- Minimum aircraft range and capacity are mandatory and must be positive values.
- The route must be uniquely identified.
- On success the system returns HTTP 201 with the created route representation.
- If either airport does not exist, the system returns HTTP 404.
- Invalid or missing required fields return HTTP 400.

---

## Pre-conditions

- The actor is authenticated as an ATCC.
- The origin and destination airports are already registered in the system.

---

## Post-conditions

- A new `Route` entity is persisted in the system.
- The route stores the estimated flight time and aircraft requirements.
- The route is available for future searches and operational planning.

---

## Main Success Scenario

1. The ATCC sends `POST /api/routes` with origin airport, destination airport, estimated flight time, and aircraft requirements.
2. The system validates the request data.
3. The system verifies that both airports exist.
4. The system creates a new `Route`.
5. The system persists the route.
6. The system returns HTTP 201 with the route representation.

---

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Missing or invalid required field | HTTP 400 |
| 2 | Estimated flight time <= 0 | HTTP 400 |
| 2 | Minimum range or capacity <= 0 | HTTP 400 |
| 3 | Origin airport does not exist | HTTP 404 |
| 3 | Destination airport does not exist | HTTP 404 |
| 3 | Origin and destination are the same | HTTP 400 |

---

## Design Justification

- `Route` is modelled as an **Aggregate Root** because it encapsulates route information and operational constraints.
- Flight time and aircraft requirements are validated as part of route creation to keep the aggregate consistent.
- Airport references are validated through the `AirportRepository` before route creation to guarantee referential consistency.

---

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us110.svg)
- [Sequence Diagram](svg/sd_us110.svg)

---
