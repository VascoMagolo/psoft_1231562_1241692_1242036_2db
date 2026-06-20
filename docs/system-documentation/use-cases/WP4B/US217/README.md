# US217 - Categorize Maintenance Records by Component

## User Story

> As a Maintenance Technician, I want to filter maintenance records by component so that I can categorize maintenance activity by area of the aircraft.

## Acceptance Criteria

- The endpoint accepts an optional `component` query parameter (ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR).
- Results are paginated.
- The response contains `MaintenanceRecordResponse` DTOs.
- If no records match the filter the response is HTTP 200 with an empty page.

## Pre-conditions

- The actor is authenticated as a Maintenance Technician or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/maintenance/records/search?component={component}`.
2. The system queries all maintenance records matching the component filter.
3. The system returns HTTP 200 with the paginated results.

## Alternative / Exception Flows

| Step | Condition                    | System Response                  |
| ---- | ---------------------------- | -------------------------------- |
| 2    | No matching records          | HTTP 200 with an empty page      |
| 1    | Invalid component enum value | HTTP 400                         |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us217.puml)
- [Sequence Diagram](puml/sd_us217.puml)
