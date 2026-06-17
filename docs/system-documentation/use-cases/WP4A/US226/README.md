# US226 - Track Maintenance Parts Inventory and Alerts

## User Story

> As a Maintenance Supervisor, I want to track maintenance parts inventory and receive low-stock alerts.

## Acceptance Criteria

### Part Registration
- The request must provide `partNumber`, `name`, `description`, `stockQuantity`, `minimumThreshold`, and `component`.
- `component` is a fixed enum value (`ENGINE`, `AIRFRAME`, `AVIONICS`, `INTERIOR`, `EXTERIOR`).
- Part numbers must be unique.
- On success the system returns HTTP 201 with the enriched `MaintenancePart` representation.

### Inventory Tracking & Alerts
- Maintenance Supervisors can search for parts by `partNumber` (partial), `name` (partial), and `component`.
- The system must provide a mechanism to filter parts in "low stock" (where `stockQuantity < minimumThreshold`).
- Search results are paginated and include HATEOAS links to update the part.

## Pre-conditions

- The actor is authenticated as a Maintenance Supervisor.
- For registration: No part with the same part number exists yet.

## Post-conditions

- New `MaintenancePart` entities are persisted.
- Inventory levels are searchable and low-stock alerts are visible to the supervisor.

## Main Success Scenario (Registration)

1. The actor sends `POST /api/maintenance/parts` with the part payload.
2. The system validates the request and checks for duplicate part numbers.
3. The system creates and persists the part.
4. The system returns HTTP 201 with the created part representation.

## Main Success Scenario (Search & Alerts)

1. The actor sends `GET /api/maintenance/parts/search?lowStock=true` to see alerts.
2. The system filters parts where current stock is below the defined threshold.
3. The system returns a paginated list of matching parts with HTTP 200.

## Alternative / Exception Flows

| Step | Condition                                 | System Response |
| ---- | ----------------------------------------- | --------------- |
| 2    | Request invalid or missing required field | HTTP 400        |
| 2    | Part number already exists (on create)    | HTTP 409        |

## Design Justification

- The inventory tracking is integrated into the search functionality using a `lowStock` query parameter, providing a unified interface for inventory management.
- The repository uses a JPQL query to handle complex filtering and threshold comparison efficiently at the database level.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us226.puml)
- [Sequence Diagram (Create)](puml/sd_us226.puml)
- [Sequence Diagram (Search & Alerts)](puml/sd_search_us226.puml)
