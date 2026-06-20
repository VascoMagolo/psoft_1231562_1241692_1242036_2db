# US227 - Calculate Fuel Efficiency Metrics

## User Story

> As an ATCC, I want to calculate fuel efficiency metrics per aircraft and per route.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- The request can optionally specify an `origin` and `destination` IATA code pair to identify a route.
- The system calculates the base fuel consumption per distance unit using the aircraft's operational range and the model's fuel capacity.
- If a route is provided, the system also calculates the total fuel needed for that specific route based on its minimum range.
- The system returns HTTP 200 with the `FuelEfficiencyResponse` DTO.
- If the aircraft does not exist, the system returns HTTP 404.
- If the route does not exist, the system returns HTTP 400.

## Pre-conditions

- The actor is authenticated as an ATCC user.
- The aircraft with the given registration exists.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/{registration}/fuel-efficiency?origin={origin}&destination={destination}`.
2. The system loads the aircraft by registration number.
3. The system calculates the base fuel efficiency.
4. If an origin and destination are provided, the system loads the route and calculates the fuel required.
5. The system returns HTTP 200 with the calculated metrics.

## Alternative / Exception Flows

| Step | Condition          | System Response |
| ---- | ------------------ | --------------- |
| 2    | Aircraft not found | HTTP 404        |
| 4    | Route not found    | HTTP 400        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us227.puml)
- [Sequence Diagram](puml/sd_us227.puml)
