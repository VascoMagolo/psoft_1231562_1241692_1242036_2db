# US207 — Register Airport with Detailed Facilities

## User Story

> As a **Backoffice Operator**, I want to register an airport with optional photos and detailed facilities information (terminals, gates, services).

## Acceptance Criteria

- All requirements from US106 apply.
- Additionally, the registration request may include: image path, services (list of strings), terminals (list of names), gates (list of identifiers).
- All facility fields are optional; the airport can be registered without them and updated later via US208.
- On success the system returns HTTP 201 with the full airport representation including facilities.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- No airport with the given IATA code exists in the system.

## Post-conditions

- A new `Airport` entity is persisted with status `OPERATIONAL`.
- All provided facilities (services, terminals, gates, image) are stored.

## Main Success Scenario

1. The actor sends `POST /api/airports` with the required fields and optional facility fields.
2. The system validates all mandatory fields (same as US106).
3. The system creates the `Airport` aggregate and immediately applies the provided facilities via `updateDetails`.
4. The system persists the airport and returns HTTP 201.

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Required field missing or invalid | HTTP 400 |
| 3 | IATA code already registered | HTTP 409 |

## Design Justification

- US207 extends US106: the same `POST /api/airports` endpoint and `RegisterAirportRequest` DTO are used. The request already supports the optional facility fields, so no separate endpoint is needed.
- Facilities are stored as `@ElementCollection` mapped to separate join tables (`airport_services`, `airport_terminals`, `airport_gates`). This avoids a separate entity lifecycle for simple list-of-string collections while still allowing efficient replacement via `updateDetails`.
- `updateDetails` applies a null-check pattern: only non-null fields are overwritten, allowing partial facility registration without clearing existing data.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us207.svg)
- [Sequence Diagram](svg/sd_us207.svg)
