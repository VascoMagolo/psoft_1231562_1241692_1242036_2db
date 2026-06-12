# US108 - Update Aircraft Model Details

## User Story

> As a Backoffice Operator, I want to update the specifications of an aircraft model to correct errors or reflect manufacturer updates.

## Acceptance Criteria

- The request must specify the model by its `modelName` in the URL.
- Optional fields for update include `maxRange`, `fuelCapacity`, `cruisingSpeed`, `maximumSeatingCapacity`, and `imagePath`.
- Numeric fields must be positive if provided.
- On success, the system returns HTTP 200 with the updated `AircraftModelResponse`.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The aircraft model with the given name exists.

## Post-conditions

- The aircraft model's specifications are updated in the database.

## Main Success Scenario

1. The actor sends `PATCH /api/aircraftModels/{modelName}` with the updated fields.
2. The system validates the request and verifies the model's existence.
3. The system updates the `AircraftModel` aggregate.
4. The system returns HTTP 200 with the updated model DTO and links.

## Alternative / Exception Flows

| Step | Condition                | System Response |
| ---- | ------------------------ | --------------- |
| 2    | Aircraft model not found | HTTP 404        |
| 2    | Invalid numeric values   | HTTP 400        |

## Design Justification

- `PATCH` allows for selective updates of model specifications.
- Consistent with the pattern used for individual aircraft updates.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us201.puml)
- [Sequence Diagram](puml/sd_us201.puml)
