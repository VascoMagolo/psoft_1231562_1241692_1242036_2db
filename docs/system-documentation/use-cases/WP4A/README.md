# WP4 - Maintenance Management
This package documents the maintenance use cases currently implemented in `src/main/java/aisafe/maintenance`.
## Covered use cases
- `US115a` - Create a maintenance record (`POST /api/maintenance/records`)
- `US115b` - Create maintenance templates (`POST /api/maintenance/templates`)
- `US116` - View all maintenance records for an aircraft (`GET /api/maintenance/records/aircraft/{registrationNumber}`)
- `US117` - View total maintenance hours for the fleet (`GET /api/maintenance/records/hours`)
- `US119` - Update maintenance record (`PATCH /api/maintenance/records/{id}`)
- `US226` - Track maintenance parts inventory (`POST /api/maintenance/parts`)
## Notes
- The implemented code uses DTOs for record listing, updating, and totals.
- `CreateMaintenanceTemplateRequest` resolves aircraft model names to entities inside the use case.
- `CreateMaintenanceRecordRequest` uses part numbers, template names, and aircraft registration numbers as simple strings.
- `US222` (due alerts) is not included here because there is no corresponding implementation in the current maintenance package yet.


