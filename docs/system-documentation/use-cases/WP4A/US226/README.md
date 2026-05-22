# US226 - Track Maintenance Parts Inventory
## User Story
> As a Maintenance Supervisor, I want to register and maintain maintenance parts so that inventory levels can be tracked manually.
## Acceptance Criteria
- The request must provide `partNumber`, `name`, `description`, `stockQuantity`, `minimumThreshold`, and `component`.
- `component` is a fixed enum value.
- Part numbers must be unique.
- On success the system returns HTTP 201 with the persisted `MaintenancePart` representation.
## Pre-conditions
- The actor is authenticated as a Maintenance Supervisor.
- No part with the same part number exists yet.
## Post-conditions
- A new `MaintenancePart` entity is persisted.
- The inventory catalog becomes available for manual quantity updates and threshold monitoring.
## Main Success Scenario
1. The actor sends `POST /api/maintenance/parts` with the part payload.
2. The system validates the request and checks for duplicate part numbers.
3. The system creates and persists the part.
4. The system returns HTTP 201 with the created part representation.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Request invalid or missing required field | HTTP 400 |
| 2 | Part number already exists | HTTP 409 |
## Design Justification
- The inventory is intentionally standalone: the current code does not couple parts to maintenance records or aircraft models.
- The controller currently returns the persisted entity, which matches the implementation.
- The `component` field uses the enum directly so the API remains consistent with the domain model.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us226.puml)
- [Sequence Diagram](puml/sd_us226.puml)


