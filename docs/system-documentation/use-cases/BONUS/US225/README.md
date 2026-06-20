# US225 - Import Bulk Data (CSV)

## User Story

> As a Backoffice Operator, I want to import data in bulk from CSV files across all domains so that I can efficiently populate the system.

## Acceptance Criteria

- CSV files are uploaded using `multipart/form-data` via domain-specific import endpoints.
- The system parses the CSV file and processes each row independently.
- If all rows succeed, the system returns HTTP 201.
- If some rows fail, the system returns HTTP 207 Multi-Status with per-row success/error details.
- If the file is invalid or empty, the system returns HTTP 400.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator or Admin.

## Post-conditions

- Valid rows are persisted as new entities in their respective domains.
- Invalid rows are reported with error details without affecting valid rows.

## Endpoints

| Domain                | Endpoint                                | Controller               |
| --------------------- | --------------------------------------- | ------------------------ |
| Aircraft Models       | `POST /api/aircraftModels/import`       | AircraftModelController  |
| Aircrafts             | `POST /api/aircrafts/import`            | AircraftController       |
| Airports              | `POST /api/airports/import`             | AirportController        |
| Routes                | `POST /api/routes/import`               | RouteController          |
| Flights               | `POST /api/flights/import`              | FlightController         |
| Maintenance Templates | `POST /api/maintenance/templates/import`| MaintenanceController    |
| Maintenance Records   | `POST /api/maintenance/records/import`  | MaintenanceController    |

## Main Success Scenario

1. The actor sends a `POST` request to the appropriate import endpoint with a CSV file attached.
2. The system validates the file format.
3. The system parses each CSV row and creates the corresponding entity.
4. The system returns HTTP 201 with a `BulkImportResult` containing the count of created entities.

## Alternative / Exception Flows

| Step | Condition                | System Response                                             |
| ---- | ------------------------ | ----------------------------------------------------------- |
| 2    | File is invalid or empty | HTTP 400                                                    |
| 3    | Some rows fail           | HTTP 207 with per-row success/error details in the response |

## Sequence Diagrams

- [SSD - Import Aircraft Models](puml/ssd_ImportAircraftModelsUseCase.puml)
- [SSD - Import Aircrafts](puml/ssd_ImportAircraftsUseCase.puml)
- [SSD - Import Airports](puml/ssd_ImportAirportsUseCase.puml)
- [SSD - Import Flights](puml/ssd_ImportFlightsUseCase.puml)
- [SSD - Import Maintenance Records](puml/ssd_ImportMaintenanceRecordsUseCase.puml)
- [SSD - Import Maintenance Templates](puml/ssd_ImportMaintenanceTemplatesUseCase.puml)
- [SSD - Import Routes](puml/ssd_ImportRoutesUseCase.puml)
- [SD - Import Aircraft Models](puml/sd_ImportAircraftModelsUseCase.puml)
- [SD - Import Aircrafts](puml/sd_ImportAircraftsUseCase.puml)
- [SD - Import Airports](puml/sd_ImportAirportsUseCase.puml)
- [SD - Import Flights](puml/sd_ImportFlightsUseCase.puml)
- [SD - Import Maintenance Records](puml/sd_ImportMaintenanceRecordsUseCase.puml)
- [SD - Import Maintenance Templates](puml/sd_ImportMaintenanceTemplatesUseCase.puml)
- [SD - Import Routes](puml/sd_ImportRoutesUseCase.puml)
