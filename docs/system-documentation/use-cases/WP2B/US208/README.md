# US208 — Update Airport Details

## User Story

> As a **Backoffice Operator**, I want to update airport details including operational hours and contact information.

## Acceptance Criteria

- Updatable fields: operational hours, contacts (type, value, description), image path, services, terminals, gates.
- All fields are optional; only provided fields are updated (partial update semantics).
- Contact types must be valid values (`PHONE`, `EMAIL`, `FAX`, etc.).
- The airport must exist.
- On success the system returns HTTP 200 with the updated airport representation.
- Airport not found returns HTTP 404.
- Concurrent update conflict returns HTTP 409.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The target airport exists.

## Post-conditions

- The airport's updatable fields are replaced with the provided values.
- Fields not present in the request remain unchanged.

## Main Success Scenario

1. The actor sends `PATCH /api/airports/{iataCode}/details` with any subset of updatable fields.
2. The system loads the airport by IATA code.
3. The system calls `airport.updateDetails(...)` with the provided values; null fields are ignored.
4. The system saves the updated airport and returns HTTP 200.

## Alternative / Exception Flows

| Step | Condition | System Response |
|------|-----------|-----------------|
| 2 | Airport not found | HTTP 404 |
| 4 | Optimistic locking version conflict | HTTP 409 |

## Design Justification

- `PATCH` is used because this is a partial update — the caller does not need to resend the full airport representation.
- `updateDetails` on the `Airport` aggregate uses null-check guards, so only explicitly provided fields are mutated. This is intentional: a `PATCH` request with `{ "operationalHours": "24/7" }` should not clear existing contacts.
- `Contact` is modelled as an embedded Value Object with a `ContactType` enum to enforce valid contact kinds at the domain level, preventing arbitrary string types.
- Optimistic locking (`@Version`) protects against lost updates when two operators edit different fields concurrently.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us208.svg)
- [Sequence Diagram](svg/sd_us208.svg)
