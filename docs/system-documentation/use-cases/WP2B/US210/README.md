# US210 — Statistics on Busiest Airports

## User Story

> As a **Backoffice Operator**, I want to generate statistics on the busiest airports by number of routes.

## Acceptance Criteria

- The system returns all airports ranked by total number of associated routes (origin + destination), in descending order.
- Each entry includes: IATA code, name, city, country, and route count.
- Airports with zero routes are included in the result.
- The system returns HTTP 200.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.

## Post-conditions

- No state change. Read-only operation.

## Main Success Scenario

1. The actor sends `GET /api/airports/statistics/busiest`.
2. The system loads all airports and all routes.
3. For each airport, the system counts how many routes reference it (as origin or destination).
4. The system sorts the result by route count descending and returns HTTP 200.

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 4 | No airports registered | HTTP 200 with empty list |

## Design Justification

- The count is computed in-memory using Java Streams (`Collectors.toMap` + `filter/count`) rather than a database aggregation query. This is acceptable for the expected data volume (hundreds of airports) and avoids a cross-context SQL join; it can be replaced with a JPQL aggregation query if performance becomes a concern.
- `AirportStatisticsResponse` is a dedicated read DTO that contains only the fields relevant to this view, following the principle of separate read models for reporting queries.
- Airports with zero routes are included intentionally — the statistic is meaningful for operators monitoring network coverage.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us210.svg)
- [Sequence Diagram](svg/sd_us210.svg)
