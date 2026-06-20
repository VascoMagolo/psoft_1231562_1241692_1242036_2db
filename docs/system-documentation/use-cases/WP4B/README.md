# WP4B - Maintenance Analytics and Reporting

This package documents the maintenance analytics and reporting use cases implemented in `src/main/java/aisafe/maintenance`.

## Covered use cases

- `US217` - Categorize maintenance records by component (`GET /api/maintenance/records/search?component={component}`)
- `US218` - Search maintenance records by filters (`GET /api/maintenance/records/search`)
- `US219` - View ongoing maintenance activities (`GET /api/maintenance/records/ongoing`)
- `US220` - Generate reports on maintenance costs (`GET /api/maintenance/records/cost/aircraft/{registrationNumber}`, `GET /api/maintenance/records/cost/model/{modelName}`)
- `US221` - View average maintenance turnaround time by model (`GET /api/maintenance/records/turnaround/model/{modelName}`)

## Notes

- US217 and US218 share the same endpoint (`/records/search`) with different filter combinations. Component filtering implements US217; multi-field filtering implements US218.
- US220 exposes two sub-endpoints: one aggregating by aircraft registration, one by aircraft model.
- All read operations in this package are available to ATCC, Maintenance Supervisor, Maintenance Technician, and Admin roles.
