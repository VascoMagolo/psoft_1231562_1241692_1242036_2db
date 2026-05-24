# WP3A - Flight Routes, Flight Operation

This package documents the flight route-related use cases currently implemented in `src/main/java/aisafe/routes`.

## Covered use cases

- `US110` - Create a flight route (`POST /api/routes`)
- `US111` - Keep track of route history (`GET /api/routes/{id}/history`)
- `US112` - Update route details & Deactivate a route (`PUT /api/routes/{id}` / `PATCH /api/routes/{id}/deactivate`)
- `US113` - View route details & View routes from airport (`GET /api/routes/{id}` / `GET /api/routes/airport/{iataCode}`)
- `US114` - Search routes (`GET /api/routes/search`)

## Supporting endpoints

- `GET /api/routes/airport/{iataCode}` returns a paginated HATEOAS list of active routes originating from a specific airport.
- `GET /api/routes/search` returns a paginated HATEOAS list of flight routes based on search criteria.

## Notes

- Route creation, tracking, updates, and searches are exposed as DTO-based read models.
- All diagrams are stored as PlantUML source in each `US` folder under `puml/`.