# US220 - Generate Maintenance Cost Reports

## User Story

> As a Maintenance Supervisor or ATCC, I want to view maintenance cost reports aggregated by aircraft or by aircraft model so that I can understand the cost distribution of maintenance operations.

## Acceptance Criteria

- Two sub-endpoints exist: one by aircraft registration, one by aircraft model name.
- The aircraft or model must exist; otherwise HTTP 404 is returned.
- The response includes total cost and average cost per record.

## Pre-conditions

- The actor is authenticated as ATCC, Backoffice Operator, Maintenance Supervisor, or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenarios

### By Aircraft

1. The actor sends `GET /api/maintenance/records/cost/aircraft/{registrationNumber}`.
2. The system locates the aircraft and aggregates costs from its maintenance records.
3. The system returns HTTP 200 with `MaintenanceCostByAircraftResponse`.

### By Model

1. The actor sends `GET /api/maintenance/records/cost/model/{modelName}`.
2. The system locates the aircraft model and aggregates costs across all aircraft of that model.
3. The system returns HTTP 200 with `MaintenanceCostByModelResponse`.

## Alternative / Exception Flows

| Step | Condition                  | System Response |
| ---- | -------------------------- | --------------- |
| 2    | Aircraft not found         | HTTP 404        |
| 2    | Aircraft model not found   | HTTP 404        |

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us220.puml)
- [Sequence Diagram](puml/sd_us220.puml)
