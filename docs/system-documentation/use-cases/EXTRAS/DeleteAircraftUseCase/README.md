# Extra - Delete Aircraft

## User Story

> As a Backoffice Operator, I want to delete an aircraft from the system when it is no longer part of the fleet.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- If the aircraft is currently assigned to any scheduled flights, the deletion may be restricted (business rule to be refined).
- On success, the system returns HTTP 204 (No Content).

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The aircraft with the given registration exists.

## Post-conditions

- The aircraft aggregate is removed from the system.

## Main Success Scenario

1. The actor sends `DELETE /api/aircrafts/{registration}`.
2. The system validates the existence of the aircraft.
3. The system removes the aircraft aggregate from the repository.
4. The system returns HTTP 204.

## Alternative / Exception Flows

| Step | Condition          | System Response |
| ---- | ------------------ | --------------- |
| 2    | Aircraft not found | HTTP 404        |

## Design Justification

- `DELETE` is the standard HTTP method for resource removal.
- Returning 204 No Content is idiomatic for successful deletions.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_deleteAircraft.puml)
- [Sequence Diagram](puml/sd_deleteAircraft.puml)

