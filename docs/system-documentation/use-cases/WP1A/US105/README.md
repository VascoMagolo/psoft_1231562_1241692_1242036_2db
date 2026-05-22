# US105 - Update Aircraft Status
## User Story
> As an ATCC user, I want to update an aircraft's status so the fleet view stays in sync with the current operational state.
## Acceptance Criteria
- The request body only needs the new status value.
- The request must include the current resource version in the `If-Match` header.
- If the version does not match, the system returns HTTP 409.
- If the status is invalid, the system returns HTTP 400.
- On success the system returns HTTP 200 with `ViewAircraftDetailsResponse` and HATEOAS links.
## Pre-conditions
- The actor is authenticated as an ATCC user.
- The target aircraft exists.
## Post-conditions
- The aircraft status is updated and the entity version is incremented.
## Main Success Scenario
1. The actor sends `PATCH /api/aircrafts/{registration}/status` with the new status and the `If-Match` header.
2. The controller parses the version from the ETag value.
3. The use case loads the aircraft and verifies the version.
4. The use case updates the status and saves the entity.
5. The system returns HTTP 200 with the updated DTO and links.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | `If-Match` header is missing or invalid | HTTP 400 |
| 3 | Aircraft not found | HTTP 404 |
| 3 | Version mismatch | HTTP 409 |
| 4 | Invalid status value | HTTP 400 |
## Design Justification
- The use case relies on optimistic locking instead of a blind overwrite.
- The response reuses `ViewAircraftDetailsResponse`, so clients receive the updated version immediately.
- The controller keeps ETag parsing outside the domain entity and passes the numeric version to the use case.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us105.puml)
- [Sequence Diagram](puml/sd_us105.puml)


