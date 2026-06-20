# US218 - Search Maintenance Records

## User Story

> As a Maintenance Supervisor or ATCC, I want to search maintenance records by multiple filters so that I can quickly locate relevant maintenance history.

## Acceptance Criteria

- The endpoint accepts optional query parameters: `registration`, `from`, `to`, `component`.
- Results are paginated.
- All parameters are combinable; an empty search returns all records.
- The response contains `MaintenanceRecordResponse` DTOs with HATEOAS links.

## Pre-conditions

- The actor is authenticated as a Maintenance Technician, Maintenance Supervisor, ATCC, or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/maintenance/records/search` with optional filter parameters.
2. The system applies all provided filters and paginates the result.
3. The system returns HTTP 200 with the paginated maintenance record list.

## Alternative / Exception Flows

| Step | Condition              | System Response                 |
| ---- | ---------------------- | ------------------------------- |
| 2    | No matching records    | HTTP 200 with an empty page     |
| 1    | Invalid date format    | HTTP 400                        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us218.puml)
- [Sequence Diagram](puml/sd_us218.puml)
