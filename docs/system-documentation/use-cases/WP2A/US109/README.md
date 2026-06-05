# US109 -- Update Airport Operational Status

## User Story

> As a **Backoffice Operator**, I want to update an airport's operational status (operational, closed, under maintenance).

## Acceptance Criteria

- Valid status values: `OPERATIONAL`, `CLOSED`, `UNDER_MAINTENANCE`.
- The airport must exist.
- On success the system returns HTTP 200 with the updated airport representation.
- Invalid or missing status returns HTTP 400.
- Airport not found returns HTTP 404.
- Concurrent update conflict returns HTTP 409.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The target airport exists.

## Post-conditions

- The `Airport` entity's status field is updated to the requested value.

## Main Success Scenario

1. The actor sends `PATCH /api/airports/{iataCode}/status` with `{ "status": "CLOSED" }`.
2. The system validates the status value.
3. The system loads the airport by IATA code.
4. The system sets the new status and saves the entity.
5. The system returns HTTP 200 with the updated airport and HATEOAS links.

## Alternative / Exception Flows

| Step | Condition                           | System Response |
| ---- | ----------------------------------- | --------------- |
| 2    | Status value missing or invalid     | HTTP 400        |
| 3    | Airport not found                   | HTTP 404        |
| 4    | Optimistic locking version conflict | HTTP 409        |

## Design Justification

- `PATCH` is used instead of `PUT` because only a single field is being updated; the full resource representation is not required in the request.
- `AirportStatus` is an enum validated at the DTO level by Jackson deserialization; invalid values fail before reaching the use case.
- Optimistic locking (`@Version` on `Airport`) ensures that two concurrent operators cannot silently overwrite each other's status change -- the second writer receives a 409 and must retry.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us109.svg)
- [Sequence Diagram](svg/sd_us109.svg)
