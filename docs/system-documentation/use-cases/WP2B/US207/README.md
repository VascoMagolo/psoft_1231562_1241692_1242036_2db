# US207 -- Register Airport with Optional Photo Upload

## User Story

> As a **Backoffice Operator**, I want to register an airport with optional photo and detailed
> facilities information (terminals, gates, services), and manage that photo independently.

## Acceptance Criteria

- A `POST /api/airports` with `Content-Type: multipart/form-data` accepts the same airport
  fields as the JSON path plus an optional `photo` file part.
- A `POST /api/airports` with `Content-Type: application/json` that includes an `image` field
  returns HTTP 400 with a message directing the client to use multipart/form-data.
- Only files with a MIME type starting with `image/` are accepted; any other type returns HTTP 400.
- `AirportResponse` carries a `hasPhoto: boolean` flag instead of raw bytes.
- `PATCH /api/airports/{iataCode}/photo` replaces or sets the photo for an existing airport
  and returns the updated `AirportResponse`.
- `GET /api/airports/{iataCode}/photo` returns the raw photo bytes with the original
  `Content-Type` header (e.g. `image/jpeg`).
- `GET /api/airports/{iataCode}/photo` on an airport with no photo returns HTTP 404.
- All facility fields (services, terminals, gates, photo) remain optional; the airport can
  be registered without them and updated later via US208 or the dedicated photo endpoint.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- No airport with the given IATA code exists in the system.
- For PATCH and GET: an airport with the given IATA code exists.

## Post-conditions

- On multipart POST: a new `Airport` entity is persisted with the optional `AirportPhoto`
  value object and status `OPERATIONAL`.
- On PATCH: the existing airport's photo is replaced in the database.
- On GET: no state change; the stored bytes are returned with the correct `Content-Type`.

## Main Success Scenario

1. The actor sends `POST /api/airports` as `multipart/form-data` with the required airport
   fields, optional runway params, optional facilities, and an optional `photo` file.
2. The system validates the MIME type if a photo was provided.
3. The system creates and persists the `Airport` aggregate with an `AirportPhoto` VO
   when a photo is present, or `null` when absent.
4. The system returns HTTP 201 with `AirportResponse` including `hasPhoto: true/false`.

## Alternative / Exception Flows

| Step | Condition                                  | System Response                                    |
| ---- | ------------------------------------------ | -------------------------------------------------- |
| 2    | Uploaded file MIME type is not `image/*`   | HTTP 400                                           |
| 2    | JSON POST includes `image` field           | HTTP 400 - hint to use multipart/form-data         |
| 2    | Required field missing or invalid          | HTTP 400                                           |
| 3    | IATA code already registered               | HTTP 409 Conflict                                  |
| -    | PATCH / GET: airport not found             | HTTP 404                                           |
| -    | GET: airport has no photo                  | HTTP 404                                           |

## Design Justification

- `AirportPhoto` is a value object because `bytes` and `contentType` are always present
  together and meaningless apart; the VO enforces that invariant in one place.
- `hasPhoto` rather than Base64 bytes in `AirportResponse` avoids inflating every list or
  detail response with potentially large binary data. Clients that need the photo call the
  dedicated `GET /{iataCode}/photo` endpoint.
- Same URL (`/api/airports`) with content-type routing (`consumes`) keeps the resource
  address consistent while allowing JSON and multipart request bodies.
- A `@Null` tombstone on the `image` field of `RegisterAirportRequest` causes the JSON
  handler's `@Valid` check to reject any client that mistakenly sends image bytes in the JSON
  body, with a descriptive error message.
- `AirportPhotoNotFoundException` extends `DomainException` but is explicitly listed in
  the `GlobalExceptionHandler` 404 handler because a missing photo on an otherwise valid
  airport is a resource-not-found condition.
- Photo management for an existing airport goes through the dedicated `PATCH /{iataCode}/photo`
  endpoint, not through `PATCH /{iataCode}/details`, mirroring the US202 pattern.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us207.svg)
- [Sequence Diagram](svg/sd_us207.svg)
