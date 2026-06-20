# US222 - Alert when Aircraft is Due for Scheduled Maintenance

## User Story

> As an ATCC, I want to see a list of aircraft that are currently due for scheduled maintenance so that I can plan maintenance checks proactively.

## Acceptance Criteria

- The endpoint `GET /api/maintenance/records/due` returns all aircraft that exceed their flight hours limit or elapsed calendar days limit since their last completed maintenance (or since their manufacturing date if no maintenance has been completed yet).
- Limits can be defined in a `MaintenanceTemplate` applicable to the aircraft model.
- If no applicable templates are found, the system falls back to global thresholds defined in `application.properties` (`aisafe.maintenance.threshold.default-flight-hours` and `aisafe.maintenance.threshold.default-days`).
- The response lists the aircraft registration number, model, due reason (including metrics), current flight hours and elapsed days, and the name of the triggering template or "Default Fallback".
- Accessible to roles: `MAINTENANCE_SUPERVISOR`, `MAINTENANCE_TECHNICIAN`, `ATCC`, `ADMIN`.

## Pre-conditions

- The actor is authenticated with one of the authorized roles.

## Post-conditions

- The list of due aircraft is calculated and returned dynamically.

## Main Success Scenario

1. The actor sends `GET /api/maintenance/records/due`.
2. The system retrieves all aircraft.
3. For each aircraft, the system finds applicable maintenance templates based on aircraft model name.
4. The system retrieves all completed maintenance records for the aircraft.
5. The system calculates the elapsed days and accumulated flight hours since the completion of the last maintenance (per template, or globally if no template applies).
6. If the elapsed days or flight hours exceed the limits (from the template or the global fallback thresholds), the aircraft is flagged as due.
7. The system returns HTTP 200 OK with the list of due aircraft.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us222.puml)
- [Sequence Diagram](puml/sd_us222.puml)
