# US228 - Export Route Network Data

## User Story

> As a Backoffice Operator, I want to export route network data in standard aviation formats (GeoJSON, KML) so that I can visualize the network in external GIS tools.

## Acceptance Criteria

- The actor can request an export via `GET /api/routes/export?format={geojson|kml}`.
- The export must include all active routes.
- Each route in the export must include origin and destination IATA codes and their corresponding coordinates.
- The system returns the data as a file download with the appropriate MIME type (`application/geo+json` or `application/vnd.google-earth.kml+xml`).
- Coordinate data must be accurately retrieved from the associated airports.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.

## Post-conditions

- A file containing the route network is generated and delivered to the actor.

## Main Success Scenario

1. The actor requests the export with a specific format.
2. The system retrieves all active routes and their endpoint coordinates.
3. The system generates the data in the requested format (GeoJSON/KML).
4. The system returns the file with a `Content-Disposition: attachment` header.

## Alternative / Exception Flows

| Step | Condition                                 | System Response |
| ---- | ----------------------------------------- | --------------- |
| 1    | Invalid format requested                  | HTTP 400        |

## Design Justification

- Providing standard formats like GeoJSON and KML enables interoperability with industry-standard mapping and analysis tools.
- Streaming the file response ensures efficient memory usage for large networks.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us228.puml)
- [Sequence Diagram](puml/sd_us228.puml)
