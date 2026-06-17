# US229 - Generate Flight Utilization Reports

## User Story

> As a Backoffice Operator, I want to generate flight utilization reports showing which routes are most frequently flown so that I can optimize the network based on demand.

## Acceptance Criteria

- The actor can request the report via `GET /api/reports/flight-utilization`.
- The actor can optionally provide `startDate` and `endDate` query parameters to filter the report.
- The report lists all routes that had at least one completed flight in the period, sorted by flight count (descending).
- Each entry in the report must include: `routeId`, `originIata`, `destinationIata`, and `completedFlightsCount`.
- The system returns HTTP 200 (OK) with the JSON report.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.

## Post-conditions

- None (Read-only reporting).

## Main Success Scenario

1. The actor requests the flight utilization report.
2. The system aggregates completed scheduled flights per route for the specified period.
3. The system sorts the results by frequency.
4. The system returns the report data.

## Alternative / Exception Flows

| Step | Condition                                 | System Response |
| ---- | ----------------------------------------- | --------------- |
| 2    | Invalid date range format                 | HTTP 400        |

## Design Justification

- This report provides direct insight into route performance, supporting data-driven network planning.
- Aggregation is performed at the database level where possible for efficiency.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us229.puml)
- [Sequence Diagram](puml/sd_us229.puml)
