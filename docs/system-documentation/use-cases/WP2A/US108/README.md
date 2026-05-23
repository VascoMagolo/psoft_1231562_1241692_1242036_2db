# US108 — Search for Airports

## User Story

> As an **ATCC**, I want to search for airports by city, country, or name.

## Acceptance Criteria

- All search parameters (name, city, country) are optional; omitting all returns all airports.
- Matching is case-insensitive and partial (contains).
- Results are paginated; default page size is 20.
- Each result item includes HATEOAS links.
- The system returns HTTP 200 even when no results match (empty page).

## Pre-conditions

- The actor is authenticated as an ATCC.

## Post-conditions

- No state change. Read-only operation.

## Main Success Scenario

1. The actor sends `GET /api/airports/search` with optional query parameters `name`, `city`, `country`, `page`, `size`, `sort`.
2. The system executes a filtered, paginated query combining any provided criteria.
3. The system returns HTTP 200 with a paginated list of airport representations, each with HATEOAS links.

## Alternative / Exception Flows

| Step | Condition                     | System Response          |
| ---- | ----------------------------- | ------------------------ |
| 3    | No airports match the filters | HTTP 200 with empty page |

## Design Justification

- The repository method `searchAirports(name, city, country, pageable)` uses a JPQL query with `LOWER(LIKE)` conditions guarded by null checks, keeping filtering logic in the persistence layer where it can be optimised.
- `Pageable` is injected via `@PageableDefault(size = 20)`, so callers do not need to specify pagination parameters unless they want to change the defaults.
- Results are mapped to `EntityModel<AirportResponse>` in the controller, adding HATEOAS links without coupling the use case to the web layer.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us108.svg)
- [Sequence Diagram](svg/sd_us108.svg)
