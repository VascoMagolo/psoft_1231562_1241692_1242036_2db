# US114 - Search Routes

## User Story

> As an **ATCC**, I want to search for routes by origin, destination, or both.

---

## Acceptance Criteria

- The system must support filtering routes by:
  - origin airport
  - destination airport
  - both origin and destination
- Search results must include route summaries.
- If no routes match the criteria, the system returns an empty list.
- On success the system returns HTTP 200.

---

## Pre-conditions

- The actor is authenticated as an ATCC.

---

## Post-conditions

- Matching routes are retrieved and presented to the actor.

---

## Main Success Scenario

1. The ATCC sends `GET /api/routes/search` with `origin`, `destination`, or both.
2. The system validates the provided filters.
3. The system retrieves matching routes.
4. The system returns HTTP 200 with the matching routes.

---

## Alternative / Exception Flows

| Step | Condition                | System Response          |
| ---- | ------------------------ | ------------------------ |
| 2    | Invalid filter format    | HTTP 400                 |
| 3    | No matching routes found | HTTP 200 with empty list |

---

## Design Justification

- Flexible filtering improves operational route discovery.
- Search operations are implemented at repository/query level for efficiency.
- The design supports future expansion with additional filters such as aircraft type or route status.

---

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us114.svg)
- [Sequence Diagram](svg/sd_us114.svg)
