# US109 - Delete Aircraft Model

## User Story

> As a Backoffice Operator, I want to delete an aircraft model that is no longer in use.

## Acceptance Criteria

- The request must specify the model by its `modelName` in the URL.
- Deletion should only be permitted if no aircraft instances of this model currently exist in the fleet (referential integrity).
- On success, the system returns HTTP 204 (No Content).

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The aircraft model with the given name exists.

## Post-conditions

- The aircraft model aggregate is removed from the system.

## Main Success Scenario

1. The actor sends `DELETE /api/aircraftModels/{modelName}`.
2. The system validates the existence of the model and checks for dependent aircraft.
3. The system removes the aircraft model aggregate from the repository.
4. The system returns HTTP 204.

## Alternative / Exception Flows

| Step | Condition                       | System Response |
| ---- | ------------------------------- | --------------- |
| 2    | Aircraft model not found        | HTTP 404        |
| 2    | Existing aircraft use this model | HTTP 409        |

## Design Justification

- Referential integrity is protected by preventing deletion of models that are still referenced by aircraft instances.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us109.puml)
- [Sequence Diagram](puml/sd_us109.puml)
