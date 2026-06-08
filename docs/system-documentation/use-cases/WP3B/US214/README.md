# US214 -- List Active Routes Sorted by Popularity or Distance

## User Story

> As an **ATCC**, I want to list all active routes sorted by popularity (number of times used) or distance.

## Acceptance Criteria

- The system must fetch only routes marked as active.
- The request must allow a `sortBy` parameter (`distance` or `popularity`).
- If sorted by distance, the calculation must use the coordinates of the associated airports.
- On success, the system returns HTTP 200 with the sorted list.

## Pre-conditions

- The actor is authenticated as an ATCC.

## Post-conditions

- None (Read-only operation).

## Main Success Scenario

1. The actor sends `GET /api/routes?status=active&sortBy={param}`.
2. The system retrieves all active routes from the repository.
3. The system calculates the distance dynamically (if requested) using the origin and destination airport coordinates.
4. The system sorts the list in memory based on the chosen criteria.
5. The system returns HTTP 200 OK with the sorted routes.

## Alternative / Exception Flows

| Step | Condition                         | System Response |
| ---- | --------------------------------- | --------------- |
| 1    | Invalid `sortBy` parameter        | HTTP 400 Bad Request |
| 3    | Associated Airport missing/deleted| HTTP 500 Internal Server Error (Data integrity issue) |

## Design Justification

- Calculating distance dynamically allows the system to remain flexible and represent a true geographical network based on the `Airport` entity's coordinates, rather than storing a static distance field that could become outdated if an airport's location is corrected.
- The sorting logic is handled in the Use Case layer to encapsulate the business logic, especially since distance calculation requires cross-referencing with the `AirportRepository`.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us214.svg)
- [Sequence Diagram](svg/sd_us214.svg)