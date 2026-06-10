# Extra - Delete Aircraft

## User Story

> As a Backoffice Operator, I want to delete an aircraft from the system when it is no longer part of the fleet.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- The aircraft cannot be deleted if it is currently assigned to any scheduled flights or has maintenance records associated with it.
- On success, the system returns HTTP 204 (No Content).

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The aircraft with the given registration exists.

## Post-conditions

- The aircraft aggregate is removed from the system.

## Main Success Scenario

1. The actor sends `DELETE /api/aircrafts/{registration}`.
2. The system validates the existence of the aircraft.
3. The system checks if the aircraft is assigned to any flights or maintenance records.
4. The system removes the aircraft aggregate from the repository.
5. The system returns HTTP 204.

## Alternative / Exception Flows

| Step | Condition                                             | System Response |
| ---- | ----------------------------------------------------- | --------------- |
| 2    | Aircraft not found                                    | HTTP 404        |
| 3    | Aircraft is assigned to flights or maintenance records | HTTP 409        |

## Design Justification

- `DELETE` is the standard HTTP method for resource removal.
- Returning 204 No Content is idiomatic for successful deletions.
- Checking for dependencies before deletion prevents generic database constraint violation errors and allows for specific domain-driven error messages (409 Conflict).

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_deleteAircraft.puml)
- [Sequence Diagram](puml/sd_deleteAircraft.puml)

