# US101 - Register Aircraft Model

## User Story

> As a Backoffice Operator, I want to register a new aircraft model so that the catalog contains the model data used by aircraft registrations.

## Acceptance Criteria

- The request must provide `modelName`, `manufacturer`, `maxRange`, `fuelCapacity`, `cruisingSpeed`, `maximumSeatingCapacity`, and `imagePath`.
- `manufacturer` is a fixed enum value, not free text.
- `modelName` must be unique; duplicate names return HTTP 409.
- Numeric fields must be positive and `maximumSeatingCapacity` must be at least 1.
- On success the system returns HTTP 201 with an `AircraftModelResponse` payload and HATEOAS links.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- No aircraft model with the same name exists yet.

## Post-conditions

- A new `AircraftModel` entity is persisted.
- The created model becomes available to aircraft registration and template selection flows.

## Main Success Scenario

1. The actor sends `POST /api/aircraftModels` with the model payload.
2. The system validates the request and checks if the model name already exists.
3. The system creates and persists the `AircraftModel` aggregate.
4. The system returns HTTP 201 with the created model DTO and links.

## Alternative / Exception Flows

| Step | Condition                                 | System Response |
| ---- | ----------------------------------------- | --------------- |
| 2    | Request invalid or missing required field | HTTP 400        |
| 2    | Model name already exists                 | HTTP 409        |

## Design Justification

- The controller returns `AircraftModelResponse`, not the entity, so the API contract stays stable.
- `Manufacturer` is serialized as an enum value, matching the code and avoiding free-text ambiguity.
- The list endpoint for aircraft models is paginated and returns lightweight DTOs as well.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us101.puml)
- [Sequence Diagram](puml/sd_us101.puml)
