# WP1B - Enhanced Aircraft Features

This package documents the enhanced aircraft-related use cases currently implemented in `src/main/java/aisafe/aircrafts`.

## Covered use cases

- `US201` - Update aircraft model details (`PATCH /api/aircraftModels/{modelName}`)
- `US202` - Register an aircraft model with an optional image or technical diagram (Covered by `US101` model registration endpoint)
- `US203` - View compatible routes for an aircraft (`GET /api/aircrafts/{registration}/compatible-routes`)
- `US204` - Top 5 most utilized aircraft models (`GET /api/aircraftModels/top-utilized`)
- `US205` - View real-time aircraft availability status (Covered by `US104` search endpoint)
- `US206` - Calculate total operational hours for an aircraft (`GET /api/aircrafts/{registration}/operational-hours`)

## Notes

- All diagrams are stored as PlantUML source in each `US` folder under `puml/`.
