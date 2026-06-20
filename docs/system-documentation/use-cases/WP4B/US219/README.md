# US219 - View Ongoing Maintenance Activities

## User Story

> As a Maintenance Supervisor or ATCC, I want to view all ongoing maintenance activities so that I can monitor what is currently in progress across the fleet.

## Acceptance Criteria

- The endpoint requires no filters.
- Results are paginated and ordered by start date descending.
- Only records with status IN_PROGRESS are included.
- The response contains `MaintenanceRecordResponse` DTOs.

## Pre-conditions

- The actor is authenticated as a Maintenance Technician, Maintenance Supervisor, ATCC, or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/maintenance/records/ongoing`.
2. The system queries all maintenance records with status IN_PROGRESS.
3. The system returns HTTP 200 with the paginated list ordered by start date descending.

## Alternative / Exception Flows

| Step | Condition                      | System Response                 |
| ---- | ------------------------------ | ------------------------------- |
| 2    | No in-progress records exist   | HTTP 200 with an empty page     |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us219.puml)
- [Sequence Diagram](puml/sd_us219.puml)
