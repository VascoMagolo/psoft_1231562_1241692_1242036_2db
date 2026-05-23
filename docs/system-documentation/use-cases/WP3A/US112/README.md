# US112 - Update or Deactivate a Route

## User Story

> As an **ATCC or Backoffice Operator**, I want to update or deactivate a route so that route information remains accurate and operationally valid.

---

## Acceptance Criteria

- Route details may be updated if the route exists.
- A route can be deactivated instead of permanently deleted.
- Deactivated routes cannot be used for operational planning.
- Every update or deactivation generates a route history entry.
- Updating route details is performed with `PUT /api/routes/{id}`.
- Deactivating a route is performed with `PATCH /api/routes/{id}/deactivate`.
- On success the system returns HTTP 200 with the updated route representation.
- If the route does not exist, the system returns HTTP 404.

---

## Pre-conditions

- The actor is authenticated as an ATCC or Backoffice Operator.
- The route exists in the system.

---

## Post-conditions

- The route information is updated or marked as inactive.
- A history entry is created describing the operation performed.

---

## Main Success Scenario

1. The actor sends `PUT /api/routes/{id}` to update route details or `PATCH /api/routes/{id}/deactivate` to deactivate the route.
2. The system validates the request.
3. The system verifies that the route exists.
4. The system updates or deactivates the route.
5. The system stores a route history entry.
6. The system returns HTTP 200 with the updated route representation.

---

## Alternative / Exception Flows

| Step | Condition            | System Response |
| ---- | -------------------- | --------------- |
| 2    | Invalid request data | HTTP 400        |
| 3    | Route not found      | HTTP 404        |

---

## Design Justification

- Soft deletion is implemented through route deactivation to preserve historical and operational consistency.
- Route updates and deactivations generate audit history entries to ensure traceability.
- Optimistic locking (`@Version`) is used on the route aggregate to prevent concurrent update conflicts.

---

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us112.svg)
- [Sequence Diagram](svg/sd_us112.svg)

---
