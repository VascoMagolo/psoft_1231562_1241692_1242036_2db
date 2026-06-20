# US205 - View Fleet Status

## User Story

> As an ATCC, I want to view the status of all aircraft in the fleet so that I can assess operational availability at a glance.

## Acceptance Criteria

- The endpoint requires no path or query parameters.
- The response groups all aircraft by their current status (AVAILABLE, INACTIVE, UNDER_MAINTENANCE, IN_FLIGHT).
- Each group includes the list of registration numbers in that status.
- The total count per group is included in the response.
- On success the system returns HTTP 200 with a `FleetStatusResponse` payload.

## Pre-conditions

- The actor is authenticated as an ATCC or Admin.

## Post-conditions

- No state change occurs; the use case is read-only.

## Main Success Scenario

1. The actor sends `GET /api/aircrafts/fleet-status`.
2. The system loads all aircraft.
3. The system groups aircraft by status and counts each group.
4. The system returns HTTP 200 with the grouped fleet status payload.

## Alternative / Exception Flows

| Step | Condition              | System Response                                |
| ---- | ---------------------- | ---------------------------------------------- |
| 2    | No aircraft registered | HTTP 200 with all groups empty (count 0 each)  |

## Design Justification

- Grouping is done in-memory over the full aircraft list; no specialised JPQL query is needed as long as the fleet size remains manageable.
- The response structure `FleetStatusResponse(groups: List<FleetStatusGroupResponse>)` allows the client to iterate over status categories without additional lookups.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us205.puml)
- [Sequence Diagram](puml/sd_us205.puml)
