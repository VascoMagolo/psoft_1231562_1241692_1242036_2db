# US221 - View Average Maintenance Turnaround Time by Model

## User Story

> As a Maintenance Supervisor or ATCC, I want to view the average turnaround time for completed maintenance records of a given aircraft model so that I can benchmark maintenance efficiency.

## Acceptance Criteria

- The path identifies the aircraft model by its model name.
- The model must exist; otherwise HTTP 404 is returned.
- Only COMPLETED maintenance records contribute to the average.
- The response includes the average turnaround in hours and the record count.

## Pre-conditions

- The actor is authenticated as a Maintenance Supervisor, Maintenance Technician, ATCC, or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/maintenance/records/turnaround/model/{modelName}`.
2. The system locates the aircraft model.
3. The system queries COMPLETED maintenance records for aircraft of that model.
4. The system calculates the average expected duration (turnaround in hours).
5. The system returns HTTP 200 with `AverageTurnaroundByModelResponse`.

## Alternative / Exception Flows

| Step | Condition                      | System Response                              |
| ---- | ------------------------------ | -------------------------------------------- |
| 2    | Aircraft model not found       | HTTP 404                                     |
| 3    | No completed records for model | HTTP 200 with average 0.0 and count 0        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us221.puml)
- [Sequence Diagram](puml/sd_us221.puml)
