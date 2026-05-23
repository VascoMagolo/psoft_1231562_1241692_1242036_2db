# WP2B - Airport Management (Extended)

This package documents the extended airport use cases currently implemented in `src/main/java/aisafe/airports`.

## Covered use cases

- `US207` - Register airport with detailed facilities (`POST /api/airports`)
- `US208` - Update airport details (`PATCH /api/airports/{iataCode}/details`)
- `US209` - View routes to/from a specific airport (`GET /api/airports/{iataCode}/routes`)
- `US210` - Stats on busiest airports (`GET /api/airports/statistics/busiest`)
- `US211` - View airports grouped by region or country (`GET /api/airports/grouped`)

## Notes

- `US209` returns `List<RouteResponse>` DTOs sourced from the routes bounded context.
- `US210` ranks airports by total number of associated routes descending.
- `US211` supports grouping by `region` (default) or `country` via query parameter.
- All diagrams are stored as PlantUML source in each `US` folder under `puml/`.
