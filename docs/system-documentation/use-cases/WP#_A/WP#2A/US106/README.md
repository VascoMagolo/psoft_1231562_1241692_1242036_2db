# US106 — Register an Airport

## User Story

> As a **Backoffice Operator**, I want to register an airport with details including IATA code, name, city, country, timezone, coordinates, and runway information.

## Acceptance Criteria

- The airport must have a unique, valid 3-character alphanumeric IATA code.
- Name, city, country, and timezone are mandatory.
- At least one runway must be provided; each runway requires a name, length (positive integer, in meters), and orientation.
- Latitude must be in [-90, 90] and longitude in [-180, 180].
- Optional fields: region, operational hours, image path, services, terminals, gates.
- On success the system returns HTTP 201 with the created airport representation including HATEOAS links.
- If an airport with the same IATA code already exists, the system returns HTTP 409.
- Invalid or missing required fields return HTTP 400 with a meaningful error message.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- No airport with the given IATA code exists in the system.

## Post-conditions

- A new `Airport` entity is persisted with status `OPERATIONAL`.
- All provided runways are stored as embedded value objects.
- Optional facilities (services, terminals, gates) are stored if provided.

## Main Success Scenario

1. The Backoffice Operator sends `POST /api/airports` with the required fields.
2. The system validates the request (IATA format, mandatory fields, runway constraints, coordinate ranges).
3. The system creates a new `Airport` aggregate with status `OPERATIONAL`.
4. The system persists the airport and returns HTTP 201 with the airport representation and HATEOAS links.

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | IATA code format invalid or missing | HTTP 400 |
| 2 | Required field missing (name, city, country, timezone) | HTTP 400 |
| 2 | No runways provided | HTTP 400 |
| 2 | Coordinates out of valid range | HTTP 400 |
| 3 | IATA code already registered | HTTP 409 |

## Design Justification

- `IataCode` is modelled as an embedded **Value Object** to encapsulate format validation (`^[A-Z0-9]{3}$`) in one place and make it reusable.
- `Runway`, `Coordinates`, `Service`, `Terminal`, and `Gate` are also Value Objects — they have no independent identity and exist only within the `Airport` aggregate.
- `Airport` is the **Aggregate Root**; all access to its parts goes through it.
- Optional facilities are accepted at registration time (via `updateDetails`) to avoid a mandatory second request for basic airports that already know their facilities.
- Optimistic locking (`@Version`) is applied to `Airport` to handle concurrent status/detail updates safely (relevant for US109/US208).

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us106.svg)
- [Sequence Diagram](svg/sd_us106.svg)
