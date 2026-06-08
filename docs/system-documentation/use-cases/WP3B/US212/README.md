# US212 -- Assign Aircraft to Route (Scheduled Flight)

## User Story

> As an **ATCC**, I want to assign an aircraft to a route for a specific date and time to create a scheduled flight. These should comply with range requirements, airplane and airport availability.

## Acceptance Criteria

- The request must include the aircraft ID, route ID, departure datetime, and arrival datetime.
- The system must validate that the route's distance does not exceed the aircraft's maximum range.
- The system must validate that the aircraft is available (no overlapping flights in the given timeframe).
- On success, the system returns HTTP 201 with the created flight representation.

## Pre-conditions

- The actor is authenticated as an ATCC (Backoffice Operator).
- Both the specified Aircraft and Route must exist in the system.

## Post-conditions

- A new `Flight` entity is persisted in the database, linking the aircraft and the route for the specified timeframe.

## Main Success Scenario

1. The actor sends `POST /api/flights` with the required assignment details.
2. The system retrieves the specified Route and Aircraft.
3. The system executes domain validations (range capacity and schedule overlap).
4. The system creates the `Flight` aggregate and persists it.
5. The system returns HTTP 201 Created with the scheduled flight details.

## Alternative / Exception Flows

| Step | Condition                         | System Response |
| ---- | --------------------------------- | --------------- |
| 2    | Route or Aircraft not found       | HTTP 404 Not Found |
| 3    | Route distance > Aircraft range   | HTTP 400 Bad Request (Business Rule Violation) |
| 3    | Aircraft schedule overlaps        | HTTP 409 Conflict (Aircraft Unavailable) |

## Design Justification

- A dedicated `FlightController` and `FlightRepository` were introduced because a Scheduled Flight is a core aggregate in this domain. Coupling it to the Route or Aircraft controllers would violate the Single Responsibility Principle.
- Validations like `hasOverlappingFlights` are executed directly at the Repository/Database level to ensure data integrity and performance, rather than loading all aircraft flights into memory.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us212.svg)
- [Sequence Diagram](svg/sd_us212.svg)