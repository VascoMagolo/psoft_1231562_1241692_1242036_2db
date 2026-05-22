# US119 - Update Maintenance Record
## User Story
> As a Maintenance Technician, I want to update a maintenance record's status and notes so that I can close or progress the work item.
## Acceptance Criteria
- The path identifies the record by ID.
- The request body requires a `status` value and may include `notes`.
- The request must include the current resource version in the `If-Match` header.
- A version mismatch returns HTTP 409.
- On success the system returns HTTP 200 with `MaintenanceRecordResponse` and HATEOAS links.
## Pre-conditions
- The actor is authenticated as a Maintenance Technician.
- The target maintenance record exists.
## Post-conditions
- The maintenance record status is updated.
- The notes are replaced only when a non-blank value is provided.
## Main Success Scenario
1. The actor sends `PATCH /api/maintenance/records/{id}` with the new status and optional notes.
2. The controller parses the version from the `If-Match` header.
3. The system loads the record by ID and checks the version.
4. The system updates the status and, when present, the notes.
5. The system saves the record and returns HTTP 200 with the response DTO.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | `If-Match` header is missing or invalid | HTTP 400 |
| 3 | Maintenance record not found | HTTP 404 |
| 3 | Version mismatch | HTTP 409 |
| 2 | Status is null | HTTP 400 |
## Design Justification
- The current request model is intentionally small: only the status is mandatory and notes are optional.
- Optimistic locking is enforced at the application layer before the save occurs.
- The returned DTO includes the updated version so clients can continue with safe concurrency control.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us119.puml)
- [Sequence Diagram](puml/sd_us119.puml)


