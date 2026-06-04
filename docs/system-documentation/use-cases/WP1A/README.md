# WP1A - Aircraft Management

This package documents the aircraft-related use cases currently implemented in `src/main/java/aisafe/aircrafts`.

## Covered use cases

- `US101` - Register aircraft model (`POST /api/aircraftModels`)
- `US102` - Register aircraft (`POST /api/aircrafts`)
- `US103` - View aircraft details (`GET /api/aircrafts/{registration}`)
- `US104` - Search aircraft (`GET /api/aircrafts/search`)
- `US105` - Update aircraft status (`PATCH /api/aircrafts/{registration}/status`)
- `US106` - Update aircraft details (`PATCH /api/aircrafts/{registration}`)
- `US107` - Delete aircraft (`DELETE /api/aircrafts/{registration}`)
- `US108` - Update aircraft model details (`PATCH /api/aircraftModels/{modelName}`)
- `US109` - Delete aircraft model (`DELETE /api/aircraftModels/{modelName}`)

## Supporting endpoints

- `GET /api/aircrafts` returns a paginated HATEOAS list of aircraft.
- `GET /api/aircraftModels` returns a paginated HATEOAS list of aircraft models.

## Notes

- Aircraft registration and listing are exposed as DTO-based read models.
- All diagrams are stored as PlantUML source in each `US` folder under `puml/`.
