# US213 -- View Scheduled Flights by Aircraft

## User Story

> As an **ATCC**, I want to view all scheduled flights for a specific aircraft.

## Acceptance Criteria

- The system must accept an aircraft identifier.
- The system must return a list of all scheduled flights associated with that aircraft.
- On success, the system returns HTTP 200 with the list.

## Pre-conditions

- The actor is authenticated as an ATCC.
- The aircraft ID provided must be valid.

## Post-conditions

- None (Read-only operation).

## Main Success Scenario

1. The actor sends `GET /api/flights?aircraftId={id}`.
2. The system validates the input and queries the database for matching flights.
3. The system maps the entities to a list of response DTOs.
4. The system returns HTTP 200 OK with the flight data.

## Alternative / Exception Flows

| Step | Condition                         | System Response |
| ---- | --------------------------------- | --------------- |
| 1    | Missing aircraftId parameter      | HTTP 400 Bad Request |
| 2    | No flights found for aircraft     | HTTP 200 OK (Empty List) |

## Design Justification

- The query is exposed via the existing `FlightController` using a query parameter (`?aircraftId=`), maintaining RESTful conventions for filtering collections.
- The database lookup is done directly via a custom query in `FlightRepository` (`findByAircraftId`), ensuring an efficient fetch without needing to load the entire Aircraft aggregate just to access its flights.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us213.svg)
- [Sequence Diagram](svg/sd_us213.svg)