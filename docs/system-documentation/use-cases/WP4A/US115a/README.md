# US115a - Create a Maintenance Record

## User Story

> As a Maintenance Technician, I want to create a maintenance record for an aircraft so that the work performed is stored with its status and duration.

## Acceptance Criteria

- The request must provide `description`, `startDate`, `expectedDuration`, `part`, `template`, `status`, and `registrationNumber`.
- `part` is the part number of an existing maintenance part.
- `template` is the name of an existing maintenance template.
- `registrationNumber` must identify an existing aircraft.
- Duplicate records with the same start date, part, and template are rejected with HTTP 409.
- On success the system returns HTTP 201 with `MaintenanceRecordResponse` and HATEOAS links.

## Pre-conditions

- The actor is authenticated as a Maintenance Technician.
- The referenced aircraft, part, and template all exist.

## Post-conditions

- A new `MaintenanceRecord` entity is persisted.
- The record is linked to the aircraft, template, and part.

## Main Success Scenario

1. The actor sends `POST /api/maintenance/records` with the record payload.
2. The system resolves the part, template, and aircraft by their identifiers.
3. The system checks whether an equivalent record already exists.
4. The system saves the record and returns HTTP 201 with the response DTO.

## Alternative / Exception Flows

| Step | Condition                        | System Response |
| ---- | -------------------------------- | --------------- |
| 2    | Part not found                   | HTTP 404        |
| 2    | Template not found               | HTTP 404        |
| 2    | Aircraft not found               | HTTP 404        |
| 3    | Equivalent record already exists | HTTP 409        |

## Design Justification

- The request uses simple string fields for part number, template name, and registration number, matching the current controller contract.
- The response is a dedicated DTO that exposes the part number, template name, aircraft registration, and entity version.
- The implementation is transactional, so all lookups and the insert happen within one unit of work.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us115a.puml)
- [Sequence Diagram](puml/sd_us115a.puml)
