# US215 -- Calculate Total Network Distance

## User Story

> As an **ATCC**, I want to calculate the total distance covered by all routes in my network.

## Acceptance Criteria

- The system must evaluate all active connections (routes) in the network.
- The total distance must be calculated dynamically based on airport coordinates.
- On success, the system returns HTTP 200 with the aggregated total and the measurement unit.

## Pre-conditions

- The actor is authenticated as an ATCC.

## Post-conditions

- None (Read-only operation).

## Main Success Scenario

1. The actor sends `GET /api/network/total-distance`.
2. The system retrieves all routes representing the network.
3. The system iterates through the routes, fetches the respective origin and destination airports, and calculates the geographic distance for each segment.
4. The system sums the distances.
5. The system returns HTTP 200 OK with the aggregated total value.

## Alternative / Exception Flows

| Step | Condition                         | System Response |
| ---- | --------------------------------- | --------------- |
| 2    | No routes exist in the network    | HTTP 200 OK (Total = 0) |

## Design Justification

- The term "network" conceptually encompasses the relationships (routes) between nodes (airports). Therefore, calculating the network distance requires retrieving route configurations and computing the Haversine distance on-the-fly using the `AirportRepository`.
- Returning a dedicated DTO (`TotalDistanceResponse`) allows the inclusion of metadata, such as the unit of measurement (e.g., kilometers or nautical miles), making the API self-documenting.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us215.svg)
- [Sequence Diagram](svg/sd_us215.svg)