# WP3 - Flight & Network Management

This package documents the flight scheduling and network calculation use cases currently implemented in `src/main/java/aisafe/flights` and `src/main/java/aisafe/routes`.

## Covered use cases

- `US212` - Assign aircraft to a route to create a scheduled flight (`POST /api/flights`)
- `US213` - View scheduled flights by aircraft (`GET /api/flights?aircraftId={id}`)
- `US214` - List active routes sorted by popularity or distance (`GET /api/routes?status=active&sortBy={param}`)
- `US215` - Calculate the total distance covered by all routes in my network (`GET /api/network/total-distance`)
- `US216` - Search for alternative routes between two airports (`GET /api/routes/alternatives?origin={iataA}&destination={iataB}`)

## Notes

- `US212` introduces `Flight` as a new aggregate root to independently manage schedule overlaps and validate aircraft range capacities against route distances.
- `US214` and `US215` calculate segment distances dynamically based on the geographic coordinates (latitude/longitude) of the associated `Airport` entities, ensuring the network state is always accurate.
- `US216` finds indirect flights (routes with layovers) between two airports, instead of just the direct connection.
- All diagrams (SSD, SD, and UCD) are stored as PlantUML source in each `US` folder under `puml/`.
