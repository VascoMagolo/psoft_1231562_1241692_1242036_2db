# Extra - Update Maintenance Components (UpdateMaintenancePartUseCase / UpdateMaintenanceTemplateUseCase)

## User Story

> As a Maintenance Technician, I want to update the details of maintenance parts and templates so that the catalog remains accurate and up to date.

## Acceptance Criteria

- The request must specify the component identifier (`partNumber` or `name` for templates) in the URL.
- For parts, the system allows updating `description`, `stockQuantity`, and `minimumThreshold`.
- For templates, the system allows updating `checklist`, `intervalFlightHours`, and `intervalDays`.
- On success, the system returns HTTP 200 with the updated component details.
- If the component does not exist, the system returns HTTP 404.

## Pre-conditions

- The actor is authenticated as a Maintenance Technician or Admin.
- The component (part or template) exists.

## Post-conditions

- The part or template details are updated in the system.

## Main Success Scenario (Updating a Part)

1. The actor sends `PATCH /api/maintenance/parts/{partNumber}` with the fields to update.
2. The system loads the part.
3. The system updates the part details.
4. The system saves the part.
5. The system returns HTTP 200 with the updated details.

## Alternative / Exception Flows

| Step | Condition          | System Response |
| ---- | ------------------ | --------------- |
| 2    | Part not found     | HTTP 404        |

## Sequence Diagrams

- [Sequence Diagram](puml/sd_updateMaintenanceComponents.puml)
