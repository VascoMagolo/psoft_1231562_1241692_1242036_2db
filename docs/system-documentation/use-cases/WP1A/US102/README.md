# US102 - Register Aircraft

## User Story

> As an ATCC user, I want to register an aircraft so that the fleet contains an operational unit linked to a model.

## Acceptance Criteria

- The request must provide `registrationNumber`, `modelId`, `manufacturingDate`, `seatCapacity`, `status`, and optional `features`.
- The registration number must follow the `XX-XXX` pattern and be unique.
- The aircraft model is resolved by `modelId`.
- The requested seat capacity cannot exceed the selected model's maximum seating capacity.
- On success the system returns HTTP 201 with `ViewAircraftDetailsResponse` and HATEOAS links.

## Pre-conditions

- The actor is authenticated as an ATCC user.
- The aircraft model referenced by `modelId` exists.

## Post-conditions

- A new `Aircraft` entity is persisted with the requested status.
- The created aircraft becomes available to list, search, and detail views.

## Main Success Scenario

1. The actor sends `POST /api/aircrafts` with the aircraft payload.
2. The system validates the request and resolves the aircraft model by ID.
3. The system checks that the registration number is new and the seat capacity does not exceed the model limit.
4. The system persists the aircraft and returns HTTP 201 with the detailed DTO and links.

## Alternative / Exception Flows

| Step | Condition                               | System Response |
| ---- | --------------------------------------- | --------------- |
| 2    | Request invalid or status missing       | HTTP 400        |
| 2    | Referenced model does not exist         | HTTP 400        |
| 3    | Registration number already exists      | HTTP 409        |
| 3    | Seat capacity exceeds the model maximum | HTTP 400        |

## Design Justification

- The use case converts the persisted entity to `ViewAircraftDetailsResponse`, keeping the API response DTO-based.
- Features are stored as a simple list of strings, matching the current code.
- The current implementation validates the status value in the use case and normalizes it to uppercase before creating the enum.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us102.puml)
- [Sequence Diagram](puml/sd_us102.puml)
