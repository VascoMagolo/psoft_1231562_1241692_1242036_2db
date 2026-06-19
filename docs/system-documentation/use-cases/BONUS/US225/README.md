# US225 - Global Bulk Import (CSV)

## 1. Context

This Use Case allows importing data in bulk from CSV files across all domains, including Airports, Aircraft Models, Aircrafts, Routes, Flights, Maintenance Templates, and Maintenance Records. It leverages the multi-status HTTP response (207) to return partial successes and errors.

## 2. Requirements

- Import CSV files using `multipart/form-data`.
- Parse CSV with OpenCSV.
- Return HTTP 201 Created if all rows succeed.
- Return HTTP 207 Multi-Status if there are partial failures.
- Return HTTP 400 Bad Request if the file is invalid.
