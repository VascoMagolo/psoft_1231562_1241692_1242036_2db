# US203 - View Compatible Routes for an Aircraft

## User Story

> As an ATCC user, I want to view a list of compatible routes for a specific aircraft to help with route assignment planning.

## Acceptance Criteria

- The request must specify the aircraft by its `registrationNumber` in the URL.
- A route is considered compatible if the aircraft's `range` is greater than or equal to the route's `minimumRange` AND the aircraft's `seatCapacity` is greater than or equal to the route's `minimumCapacity`.
- The system returns HTTP 200 with a list of compatible `Route` DTOs.
- If the aircraft does not exist, the system returns HTTP 404.

## Pre-conditions

- The actor is authenticated as an ATCC user.
- The aircraft with the given registration exists.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/{registration}/compatible-routes`.
2. The system loads the aircraft by registration number.
3. The system queries the `RouteRepository` for compatible routes based on the aircraft's range and capacity.
4. The system maps the routes to DTOs.
5. The system returns HTTP 200 with the list of DTOs.

## Alternative / Exception Flows

| Step | Condition          | System Response |
| ---- | ------------------ | --------------- |
| 2    | Aircraft not found | HTTP 404        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us203.puml)
- [Sequence Diagram](puml/sd_us203.puml)
