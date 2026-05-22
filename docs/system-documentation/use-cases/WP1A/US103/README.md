# US103 - View Aircraft Details
## User Story
> As an ATCC user or Backoffice Operator, I want to view the details of an aircraft by registration number.
## Acceptance Criteria
- The request path contains the aircraft registration number.
- The system returns the complete `ViewAircraftDetailsResponse` payload, including the version used for optimistic locking.
- If the aircraft does not exist, the system returns HTTP 404.
- On success the system returns HTTP 200 with HATEOAS links.
## Pre-conditions
- The actor is authenticated as an ATCC user or Backoffice Operator.
## Post-conditions
- No state change occurs; the use case is read-only.
## Main Success Scenario
1. The actor sends `GET /api/aircrafts/{registration}`.
2. The system loads the aircraft by registration number.
3. The system maps the aggregate to `ViewAircraftDetailsResponse`.
4. The system returns HTTP 200 with the DTO and links.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Aircraft not found | HTTP 404 |
## Design Justification
- The controller uses the `RegistrationNumber` value object at the boundary, so the format validation stays in the domain layer.
- The response includes status and version so clients can safely perform later updates with optimistic locking.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us103.puml)
- [Sequence Diagram](puml/sd_us103.puml)


