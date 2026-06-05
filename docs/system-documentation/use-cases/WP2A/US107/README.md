# US107 -- View Airport Details

## User Story

> As a **Backoffice Operator** or **ATCC**, I want to view an airport's details given its IATA code.

## Acceptance Criteria

- The system returns full airport details: IATA code, name, city, country, region, timezone, coordinates, status, operational hours, image, runways, contacts, services, terminals, and gates.
- The response includes HATEOAS links: self, update-status, update-details, routes, certifications.
- If the airport is not found, the system returns HTTP 404.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator or ATCC.

## Post-conditions

- No state change. Read-only operation.

## Main Success Scenario

1. The actor sends `GET /api/airports/{iataCode}`.
2. The system looks up the airport by the given IATA code (case-insensitive, normalised to uppercase).
3. The system returns HTTP 200 with the airport representation and HATEOAS links.

## Alternative / Exception Flows

| Step | Condition         | System Response |
| ---- | ----------------- | --------------- |
| 2    | Airport not found | HTTP 404        |

## Design Justification

- The IATA code is normalised to uppercase at the controller boundary (`iataCode.toUpperCase()`), keeping the domain invariant (`^[A-Z0-9]{3}$`) without burdening callers to send uppercase.
- `AirportResponse` is a flat DTO assembled from the aggregate and its value object collections; it decouples the API contract from the internal domain model.
- HATEOAS links are built using Spring HATEOAS `WebMvcLinkBuilder`, pointing to the actions available on the returned resource.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us107.svg)
- [Sequence Diagram](svg/sd_us107.svg)
