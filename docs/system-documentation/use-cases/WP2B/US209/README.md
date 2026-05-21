# US209 — View Routes to/from an Airport

## User Story

> As an **ATCC**, I want to view all routes that depart from or arrive at a specific airport.

## Acceptance Criteria

- The system returns all routes where the airport is either the origin or the destination.
- The airport must exist; otherwise HTTP 404 is returned.
- An empty list is returned if the airport has no associated routes.
- The system returns HTTP 200.

## Pre-conditions

- The actor is authenticated as an ATCC.
- The target airport exists.

## Post-conditions

- No state change. Read-only operation.

## Main Success Scenario

1. The actor sends `GET /api/airports/{iataCode}/routes`.
2. The system verifies the airport exists.
3. The system queries routes where `originAirport.iataCode = iataCode OR destinationAirport.iataCode = iataCode`.
4. The system returns HTTP 200 with the list of routes.

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Airport not found | HTTP 404 |
| 4 | Airport exists but has no routes | HTTP 200 with empty list |

## Design Justification

- Airport existence is verified before querying routes (`existsByIataCodeCode`) to fail fast with a meaningful 404 rather than silently returning an empty list for a nonexistent airport.
- The route query crosses bounded context boundaries (Airports → Routes). Rather than coupling the Airport aggregate to Route entities, the `ViewAirportRoutesUseCase` depends on both `AirportRepository` and `RouteRepository` directly — a deliberate application-layer cross-context coordination.
- `Route` entities are returned directly (not wrapped in a DTO) at this stage; this can be refined if the Route representation needs to be projected differently for this endpoint.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us209.svg)
- [Sequence Diagram](svg/sd_us209.svg)
