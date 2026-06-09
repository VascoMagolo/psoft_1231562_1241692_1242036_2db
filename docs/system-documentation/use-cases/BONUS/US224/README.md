# US224 - Search Aircraft by Features

## User Story

> As an ATCC, I want to search for aircraft by specific features (e.g., WiFi-enabled, specific engine type).

## Acceptance Criteria

- The search accepts a `feature` query parameter.
- Results are paginated.
- The response is a page of `SearchAircraftUseCaseResponse` DTOs.
- If no feature is provided, the system returns HTTP 400.
- If no aircraft matching the feature are found, an empty page is returned with HTTP 200.

## Pre-conditions

- The actor is authenticated as an ATCC user.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/search/feature?feature=WiFi`.
2. The system validates that the feature parameter is present.
3. The system executes the paginated search for aircraft containing that specific feature.
4. The system maps the results to lightweight search DTOs.
5. The system returns HTTP 200 with the paged HATEOAS model.

## Alternative / Exception Flows

| Step | Condition                     | System Response             |
| ---- | ----------------------------- | --------------------------- |
| 2    | Feature parameter is missing  | HTTP 400                    |
| 3    | No aircraft match the feature | HTTP 200 with an empty page |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us224.puml)
- [Sequence Diagram](puml/sd_us224.puml)
