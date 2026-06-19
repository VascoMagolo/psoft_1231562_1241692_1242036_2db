# US202 - Register Aircraft Model with Optional Image

## User Story

> As a Backoffice Operator, I want to register an aircraft model with an optional image and manage
> that image independently, so that the catalog can display aircraft model photos alongside
> technical specifications.

## Acceptance Criteria

- A `POST /api/aircraftModels` with `Content-Type: multipart/form-data` accepts the same model
  fields as the JSON path plus an optional `image` file part.
- A `POST /api/aircraftModels` with `Content-Type: application/json` that includes an `image`
  field returns HTTP 400 with a message directing the client to use multipart/form-data.
- Only files with a MIME type starting with `image/` are accepted; any other type returns HTTP 400.
- `AircraftModelResponse` carries a `hasImage: boolean` flag instead of raw bytes (no Base64
  in the main response).
- `PATCH /api/aircraftModels/{modelName}/image` replaces or sets the image for an existing model
  and returns the updated `AircraftModelResponse`.
- `GET /api/aircraftModels/{modelName}/image` returns the raw image bytes with the original
  `Content-Type` header (e.g. `image/jpeg`).
- `GET /api/aircraftModels/{modelName}/image` on a model with no image returns HTTP 404.

## Pre-conditions

- The actor is authenticated as a Backoffice Operator.
- For PATCH and GET: an aircraft model with the given name exists.

## Post-conditions

- On multipart POST: a new `AircraftModel` is persisted with the optional `AircraftModelImage`
  value object.
- On PATCH: the existing model's image is replaced in the database.
- On GET: no state change; the stored bytes are returned as-is.

## Main Success Scenario

1. The actor sends `POST /api/aircraftModels` as `multipart/form-data` with model fields and an
   optional `image` file.
2. The system validates the MIME type (if an image was provided).
3. The system creates and persists the `AircraftModel` aggregate with an `AircraftModelImage` VO
   when an image is present, or `null` when absent.
4. The system returns HTTP 201 with `AircraftModelResponse` including `hasImage: true/false`.

## Alternative / Exception Flows

| Step | Condition                                  | System Response                                        |
| ---- | ------------------------------------------ | ------------------------------------------------------ |
| 2    | Uploaded file MIME type is not `image/*`   | HTTP 400                                               |
| 2    | JSON POST includes `image` field           | HTTP 400 - hint to use multipart/form-data             |
| 3    | Model name already exists                  | HTTP 409 Conflict                                      |
| -    | PATCH / GET: model name not found          | HTTP 404                                               |
| -    | GET: model has no image                    | HTTP 404                                               |

## Design Justification

- `AircraftModelImage` is a value object because `bytes` and `contentType` are always present
  together and meaningless apart; the VO enforces that invariant in one place.
- `hasImage` rather than Base64 bytes in `AircraftModelResponse` avoids inflating every list or
  detail response with potentially large binary data. Clients that need the image call the
  dedicated `GET /{modelName}/image` endpoint.
- Same URL (`/api/aircraftModels`) with content-type routing (`consumes`) keeps the resource
  address consistent while allowing two different request bodies. Spring MVC dispatches by
  `Content-Type` before reaching controller code.
- A `@Null` tombstone on the `image` field of `RegisterAircraftModelRequest` causes the JSON
  handler's `@Valid` check to reject any client that mistakenly sends image bytes in the JSON
  body, with a descriptive error message.
- `AircraftModelImageNotFoundException` extends `DomainException` but is explicitly listed in
  the `GlobalExceptionHandler` 404 handler (not the generic 400 handler) because a missing
  image on an otherwise valid model is a resource-not-found condition.

## Sequence Diagrams

- [System Sequence Diagram](puml/ssd_us202.puml)
- [Sequence Diagram](puml/sd_us202.puml)
