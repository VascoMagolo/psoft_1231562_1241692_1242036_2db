# US111 - Keep Track of Route History

## User Story

> As an **ATCC**, I want to keep track of route history so that I can audit route changes and operational decisions over time.

---

## Acceptance Criteria

- Every route update or deactivation must generate a history entry.
- History entries must include timestamp, operation type, and modified fields.
- Route history must be retrievable by route ID.
- History entries are immutable.
- On success the system returns HTTP 200 with the route history.
- If the route does not exist, the system returns HTTP 404.

---

## Pre-conditions

- The actor is authenticated as an ATCC.
- The route exists in the system.

---

## Post-conditions

- Route history entries are persisted after every update or deactivation performed in US112.
- Historical data remains available for auditing purposes.

---

## Main Success Scenario

1. The ATCC requests `GET /api/routes/{id}/history`.
2. The system validates the route existence.
3. The system retrieves all history entries associated with the route.
4. The system returns HTTP 200 with the route history.

---

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Route does not exist | HTTP 404 |
| 3 | No history entries found | HTTP 200 with empty list |

---

## Design Justification

- `RouteHistory` is modelled as a separate entity because historical records require identity and persistence over time.
- History entries are immutable to preserve audit integrity.
- The audit mechanism is triggered by update/deactivation operations and can be queried independently.

---

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us111.svg)
- [Sequence Diagram](svg/sd_us111.svg)

---
