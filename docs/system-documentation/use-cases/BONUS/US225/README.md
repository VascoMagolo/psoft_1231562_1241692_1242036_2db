# US225 - Import Bulk Airport Data

## User Story

> As a Backoffice Operator, I want to import bulk airport data from CSV files so that I can quickly populate the system with many airports without manual entry.

## Acceptance Criteria

- The request must accept a CSV file upload via `POST /api/airports/import`.
- The CSV must contain columns for `iataCode`, `name`, `city`, `country`, `timezone`, `latitude`, `longitude`.
- Rows with invalid data (e.g. invalid IATA code format) must be rejected and reported.
- If an airport with the same IATA code already exists, that row should be ignored or reported as a conflict.
- The system returns HTTP 201 (Created) if all rows are imported, or 207 (Multi-Status) if there are partial successes/failures.
- A summary of imported, skipped, and failed records is returned in the response.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- The provided file is a valid CSV format.

## Post-conditions

- New `Airport` entities are persisted in the system.

## Main Success Scenario

1. The actor uploads a CSV file to the import endpoint.
2. The system parses the CSV and validates each row against domain rules.
3. The system creates and persists `Airport` aggregates for each valid row.
4. The system returns the import summary and status.

## Alternative / Exception Flows

| Step | Condition                                 | System Response |
| ---- | ----------------------------------------- | --------------- |
| 2    | File is empty or malformed                | HTTP 400        |
| 3    | All IATA codes already exist              | HTTP 409        |

## Design Justification

- Bulk import reduces administrative overhead.
- Multi-Status (207) response allows the actor to see exactly which rows failed without failing the entire batch.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us225.puml)
- [Sequence Diagram](puml/sd_us225.puml)
