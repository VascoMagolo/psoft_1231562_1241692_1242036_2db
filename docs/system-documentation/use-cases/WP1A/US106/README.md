# US106 - Update Aircraft Details

## User Story

> As a Backoffice Operator, I want to update the details of an existing aircraft so that its technical specifications and features remain accurate.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- Optional fields for update include `modelName`, `manufacturingDate`, `seatCapacity`, and `features`.
- If `modelName` is provided, the system must verify its existence.
- The new `seatCapacity` cannot exceed the maximum capacity of the aircraft's model.
- Optimistic concurrency control is implemented using the `If-Match` header (mapping to the aircraft's `version`).
- On success, the system returns HTTP 200 with the updated `ViewAircraftDetailsResponse`.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The aircraft with the given registration exists.
- The `If-Match` header matches the current version of the aircraft entity.

## Post-conditions

- The aircraft's details are updated in the database.
- The version of the aircraft aggregate is incremented.

## Main Success Scenario

1. The actor sends `PATCH /api/aircrafts/{registration}` with the updated fields and `If-Match` header.
2. The system validates the request and checks for concurrency conflicts.
3. The system updates the `Aircraft` aggregate.
4. The system returns HTTP 200 with the updated model and HATEOAS links.

## Alternative / Exception Flows

| Step | Condition                                | System Response |
| ---- | ---------------------------------------- | --------------- |
| 2    | Aircraft not found                       | HTTP 404        |
| 2    | Concurrency conflict (If-Match mismatch) | HTTP 412        |
| 2    | Invalid model name                       | HTTP 404        |
| 2    | Seat capacity exceeds model maximum      | HTTP 400        |

## Design Justification

- `PATCH` is used to allow partial updates of the aircraft resource.
- Optimistic locking ensures that concurrent updates do not overwrite each other silently.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us106.puml)
- [Sequence Diagram](puml/sd_us106.puml)
