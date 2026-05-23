# WP2A - Airport Management

This package documents the airport-related use cases currently implemented in `src/main/java/aisafe/airports`.

## Covered use cases

- `US106` - Register an airport (`POST /api/airports`)
- `US106a` - Add aircraft certification to airport (`POST /api/airports/{iataCode}/certifications`)
- `US107` - View airport details (`GET /api/airports/{iataCode}`)
- `US108` - Search for airports (`GET /api/airports/search`)
- `US109` - Update airport operational status (`PATCH /api/airports/{iataCode}/status`)

## Notes

- All endpoints return HATEOAS `EntityModel<AirportResponse>` with links to related actions.
- Airport search supports optional filtering by name, city, and country with pagination.
- All diagrams are stored as PlantUML source in each `US` folder under `puml/`.
