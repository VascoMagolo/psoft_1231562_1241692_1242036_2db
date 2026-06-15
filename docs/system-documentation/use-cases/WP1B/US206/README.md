# US206 - Calculate Total Operational Hours

## User Story

> As a Backoffice Operator, I want to calculate the total operational hours for an aircraft based on its flight history to schedule maintenance appropriately.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- The operational hours are calculated as the sum of all flight durations for that aircraft.
- The calculation is performed via a Native SQL aggregation query for performance.
- The result is returned as a number (representing hours).
- If the aircraft does not exist, returns HTTP 404.
- If the aircraft exists but has no flights, returns 0.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/{registration}/operational-hours`.
2. The system checks if the aircraft exists.
3. The system queries the `ScheduledFlightRepository` to sum flight durations.
4. The system returns HTTP 200 with the numeric result.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us206.puml)
- [Sequence Diagram](puml/sd_us206.puml)
