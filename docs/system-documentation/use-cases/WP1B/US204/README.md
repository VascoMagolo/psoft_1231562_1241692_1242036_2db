# US204 - Top 5 Most Utilized Aircraft Models

## User Story

> As a Backoffice Operator, I want to view the top 5 most utilized aircraft models so that I can analyze fleet utilization trends.

## Acceptance Criteria

- The request can filter by a `criteria` query parameter: either `HOURS` (flight hours) or `ASSIGNMENTS` (number of flight assignments).
- Defaults to `HOURS` if no criteria is provided.
- Returns a list of up to 5 aircraft models with their corresponding utilization metric.
- Aggregation is performed at the database level.
- The system returns HTTP 200.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircraftModels/top-utilized?criteria=HOURS`.
2. The system delegates to the use case.
3. The use case calls the appropriate aggregation method on the `ScheduledFlightRepository`.
4. The system returns HTTP 200 with the list of the top 5 models and their scores.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us204.puml)
- [Sequence Diagram](puml/sd_us204.puml)
