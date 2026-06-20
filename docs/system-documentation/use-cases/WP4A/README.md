# WP4A - Maintenance Records (Core)

This package documents the maintenance use cases currently implemented in `src/main/java/aisafe/maintenance`.

## Covered use cases

- `US115a` - Create a maintenance record (`POST /api/maintenance/records`)
- `US115b` - Create maintenance templates (`POST /api/maintenance/templates`)
- `US116` - View all maintenance records for an aircraft (`GET /api/maintenance/records/aircraft/{registrationNumber}`)
- `US117` - View total maintenance hours for the fleet (`GET /api/maintenance/records/hours`)
- `US119` - Update maintenance record (`PATCH /api/maintenance/records/{id}`)
- `US222` - Alert when aircraft due for scheduled maintenance (`GET /api/maintenance/records/due`)
- `US226` - Track maintenance parts inventory (`POST /api/maintenance/parts`)

## Notes

- The implemented code uses DTOs for record listing, updating, and totals.
- `CreateMaintenanceTemplateRequest` resolves aircraft model names to entities inside the use case.
- `CreateMaintenanceRecordRequest` uses part numbers, template names, and aircraft registration numbers as simple strings.
- `US222` is fully implemented to dynamically alert when aircraft are due for scheduled maintenance based on flight hours or elapsed calendar days.
- `US118` (delete maintenance record) is descoped and not implemented.
- `US222` and `US226` are Phase 2 features; see `WP4B` for the full Phase 2 analytics package.
