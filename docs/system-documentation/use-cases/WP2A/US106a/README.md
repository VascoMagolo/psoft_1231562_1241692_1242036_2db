# US106a -- Add Aircraft Certification to Airport

## User Story

> As a **Backoffice Operator** or **ATCC**, I want to add an aircraft certification to an airport, indicating that a particular aircraft model can fly to/from that airport.

## Acceptance Criteria

- The airport identified by the given IATA code must exist.
- The aircraft model identified by the given ID must exist.
- The same aircraft model cannot be certified for the same airport more than once.
- On success the system returns HTTP 201 with the certification representation and a HATEOAS link back to the airport.
- Airport not found returns HTTP 404; aircraft model not found returns HTTP 404.
- Duplicate certification returns HTTP 409 with a meaningful error message.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator or ATCC.
- The target airport exists.
- The target aircraft model exists.

## Post-conditions

- A new `AircraftCertification` entity is persisted linking the airport to the aircraft model.

## Main Success Scenario

1. The actor sends `POST /api/airports/{iataCode}/certifications` with `{ "aircraftModelId": <id> }`.
2. The system looks up the airport by IATA code.
3. The system looks up the aircraft model by ID.
4. The system checks no certification already exists for this airport-model pair.
5. The system persists the certification and returns HTTP 201.

## Alternative / Exception Flows

| Step | Condition                    | System Response |
| ---- | ---------------------------- | --------------- |
| 2    | Airport not found            | HTTP 404        |
| 3    | Aircraft model not found     | HTTP 404        |
| 4    | Certification already exists | HTTP 409        |

## Design Justification

- `AircraftCertification` is modelled as a separate **Entity** (own identity, own repository) rather than a collection on `Airport`, because certifications may eventually be queried independently (e.g., "which airports certify model X?").
- Duplicate detection uses a repository query (`existsByAirportAndAircraftModelId`) rather than a unique constraint alone, to produce a domain-meaningful 409 error message instead of a generic DB exception.
- `AircraftNotFoundException` (HTTP 404) and `DuplicateResourceException` (HTTP 409) are thrown instead of a generic `DomainException`, aligning error codes with the OpenAPI documentation and REST semantics.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us106a.svg)
- [Sequence Diagram](svg/sd_us106a.svg)
