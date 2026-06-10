# US104 - Search Aircraft

## User Story

> As an ATCC user, I want to search aircraft by model, status, manufacturing year, and specific features so I can quickly filter the fleet. (Note: Incorporates requirements of bonus US224).

## Acceptance Criteria

- The search accepts optional `modelName`, `status`, `year`, and `feature` query parameters.
- Results are paginated.
- The response is a page of `SearchAircraftUseCaseResponse` DTOs.
- An empty result set still returns HTTP 200.

## Pre-conditions

- The actor is authenticated as an ATCC user.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/search` with any subset of the supported filters.
2. The system executes the paginated repository search.
3. The system maps the page to lightweight search DTOs.
4. The system returns HTTP 200 with a paged HATEOAS model.

## Alternative / Exception Flows

| Step | Condition                     | System Response             |
| ---- | ----------------------------- | --------------------------- |
| 2    | No aircraft match the filters | HTTP 200 with an empty page |

## Design Justification

- The controller keeps the paging contract by using `Pageable` and `PagedResourcesAssembler`.
- The search output is a lightweight DTO so the API only exposes the fields needed for the listing screen.
- Merged with US224 to provide a single, unified search endpoint for aircraft.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us104.puml)
- [Sequence Diagram](puml/sd_us104.puml)
