# US115b - Create Maintenance Templates
## User Story
> As a Maintenance Technician, I want to create maintenance templates so that recurring maintenance work can be reused across compatible aircraft models.
## Acceptance Criteria
- The request must provide `name`, `templateType`, `applicableModels`, `checklist`, `intervalFlightHours`, and `intervalDays`.
- `applicableModels` is a list of aircraft model names, not IDs.
- Each model name must exist in the aircraft model catalog.
- Template names must be unique.
- On success the system returns HTTP 201 with the persisted `MaintenanceTemplate` representation.
## Pre-conditions
- The actor is authenticated as a Maintenance Technician.
- All referenced aircraft model names exist.
## Post-conditions
- A new `MaintenanceTemplate` entity is persisted with its applicable models and checklist.
## Main Success Scenario
1. The actor sends `POST /api/maintenance/templates` with the template payload.
2. The system resolves each model name to an `AircraftModel` entity.
3. The system checks whether a template with the same name already exists.
4. The system saves the template and returns HTTP 201.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | One of the model names does not exist | HTTP 404 |
| 3 | Template name already exists | HTTP 409 |
## Design Justification
- The use case keeps the request model simple by accepting model names and resolving them at the application layer.
- Intervals are stored on the template, which matches the current code and the maintenance clarification rules.
- The current controller returns the persisted domain entity, so the documentation reflects the API as implemented.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us115b.puml)
- [Sequence Diagram](puml/sd_us115b.puml)


