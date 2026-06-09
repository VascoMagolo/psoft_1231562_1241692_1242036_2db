# US223 - Aircraft Utilization Rates Graphs

## User Story

> As an ATCC, I want to view aircraft utilization rates over time with graphical representations.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- The request must specify a date range (`startDate` and `endDate`).
- The system returns daily data points containing flight hours and utilization rate percentage.
- The response should be a collection of points suitable for plotting graphical representations.
- If the aircraft does not exist, the system returns HTTP 404.
- On success, the system returns HTTP 200 with the data points.

## Pre-conditions

- The actor is authenticated as an ATCC user.
- The aircraft with the given registration exists.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/{registration}/utilization?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`.
2. The system loads the scheduled flights for the given aircraft in the date range.
3. The system aggregates flight hours by day.
4. The system calculates the daily utilization percentage.
5. The system returns HTTP 200 with a list of daily data points.

## Alternative / Exception Flows

| Step | Condition          | System Response |
| ---- | ------------------ | --------------- |
| 2    | Aircraft not found | HTTP 404        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us223.puml)
- [Sequence Diagram](puml/sd_us223.puml)
