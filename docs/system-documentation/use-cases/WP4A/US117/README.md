# US117 - View Total Maintenance Hours for the Fleet
## User Story
> As a Maintenance Supervisor, I want to view the total maintenance hours for the fleet so that I can understand the overall maintenance effort.
## Acceptance Criteria
- The system returns a single `ViewTotalMaintenanceHoursinFleetResponse` payload.
- The total is calculated from the sum of all maintenance record expected durations.
- The endpoint always returns HTTP 200.
## Pre-conditions
- The actor is authenticated as a Maintenance Supervisor or authorised maintenance user.
## Post-conditions
- No state change occurs; the use case is read-only.
## Main Success Scenario
1. The actor sends `GET /api/maintenance/records/hours`.
2. The system loads all maintenance records.
3. The system sums the expected duration of every record.
4. The system returns HTTP 200 with the total hours DTO.
## Alternative / Exception Flows
| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | No maintenance records exist | HTTP 200 with total hours equal to 0 |
## Design Justification
- The current implementation uses an in-memory stream sum over `expectedDuration`, which matches the code and keeps the endpoint simple.
- The response is a dedicated DTO with a single `totalHours` field.
## Sequence Diagrams
- [System Sequence Diagram](puml/ssd_us117.puml)
- [Sequence Diagram](puml/sd_us117.puml)


