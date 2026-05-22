# US116 - View All Maintenance Records for Aircraft
## User Story
> As a Maintenance Technician, I want to view the maintenance records of a specific aircraft so that I can review its maintenance history.
## Acceptance Criteria
- The path includes the aircraft registration number.
- The aircraft must exist; otherwise HTTP 404 is returned.
- The result is paginated.
- The response is a page of `ViewAllMaintenanceRecordsResponse` DTOs.
## Pre-conditions
- The actor is authenticated as a Maintenance Technician.
- The target aircraft exists.
## Post-conditions
- No state change occurs; the use case is read-only.
## Main Success Scenario
1. The actor sends `GET /api/maintenance/records/aircraft/{registrationNumber}`.
2. The system validates that the aircraft exists.
3. The system loads the aircraft's maintenance records using pagination.
4. The system returns HTTP 200 with the paged DTO list.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Aircraft not found | HTTP 404 |
| 3 | The aircraft has no records | HTTP 200 with an empty page |
## Design Justification
- The use case returns a `Page<ViewAllMaintenanceRecordsResponse>` so the controller can expose a paginated read model.
- Aircraft existence is verified before querying records, which gives a clean 404 instead of an empty page for invalid registrations.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us116.puml)
- [Sequence Diagram](puml/sd_us116.puml)


