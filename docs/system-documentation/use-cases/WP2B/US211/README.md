# US211 -- View Airports Grouped by Region or Country

## User Story

> As an **ATCC**, I want to view airports grouped by region or country.

## Acceptance Criteria

- The default grouping is by region; passing `?by=country` groups by country instead.
- Groups are returned sorted alphabetically by group key.
- Airports without a region are placed in a group labelled `"Unknown"`.
- Each group contains the full airport representations.
- The system returns HTTP 200.

## Pre-conditions

- The actor is authenticated as an ATCC.

## Post-conditions

- No state change. Read-only operation.

## Main Success Scenario

1. The actor sends `GET /api/airports/grouped` with optional `?by=region` (default) or `?by=country`.
2. The system loads all airports.
3. The system partitions the airports using `Collectors.groupingBy` on the chosen field.
4. The system sorts groups alphabetically and returns HTTP 200 with a list of `{ groupKey, airports[] }` objects.

## Alternative / Exception Flows

| Step | Condition                              | System Response                     |
| ---- | -------------------------------------- | ----------------------------------- |
| 3    | No airports registered                 | HTTP 200 with empty list            |
| 3    | Airport has no region (region is null) | Airport placed in `"Unknown"` group |

## Design Justification

- Grouping is performed in-memory with Java Streams. For the expected airport count (hundreds), this avoids the complexity of a `GROUP BY` query while keeping the logic straightforward and readable.
- The `by` parameter uses a simple string comparison (`"country".equalsIgnoreCase`) rather than an enum to keep the endpoint flexible and backward-compatible if new grouping criteria are added.
- `AirportGroupResponse` is a dedicated DTO pairing a group key string with a list of `AirportResponse` items, cleanly representing the grouped structure without exposing domain internals.
- The `"Unknown"` fallback for null regions prevents a `NullPointerException` in `groupingBy` and provides a meaningful bucket for airports not yet assigned to a region.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us211.svg)
- [Sequence Diagram](svg/sd_us211.svg)
