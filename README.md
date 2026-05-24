# AISafe

A RESTful backend for aviation safety management - aircraft, airports, flight routes, and maintenance records.

## About

AISafe is designed to manage the operational data of an airline organisation. It provides a structured API that any frontend (web or mobile) can consume to:

- Register and track **aircraft models** and individual **aircraft** instances across their full lifecycle
- Manage **airport** information including runways, facilities, certifications, and operational status
- Define and maintain **flight routes** between airports, including history of changes
- Schedule and record **maintenance** operations with templates, parts inventory, and technician notes

The system is built around **role-based access control** with five distinct roles:

| Role                   | Description                                        |
|------------------------|----------------------------------------------------|
| Admin                  | Full system access                                 |
| Backoffice Operator    | Airport and aircraft management                    |
| ATCC                   | Air Traffic Control - route and airport visibility |
| Maintenance Technician | Record maintenance operations                      |
| Maintenance Supervisor | Manage parts, templates, and maintenance oversight |

All endpoints are authenticated via **JWT** and return **HAL-compliant** responses (HATEOAS) with links to related resources.

## Tech Stack

| Layer       | Technology                                           |
|-------------|------------------------------------------------------|
| Language    | Java                                                 |
| Framework   | Spring Boot                                          |
| Persistence | Spring Data JPA + Hibernate + H2 (in-memory)         |
| Security    | Spring Security + JWT                                |
| API style   | REST + HATEOAS (Spring HATEOAS)                      |
| API docs    | SpringDoc OpenAPI (Swagger UI)                       |
| Validation  | Jakarta Validation + Hibernate Validator             |
| Utilities   | Lombok                                               |
| Build       | Maven (wrapper included - no local install required) |

## Architecture

Requests flow through three layers within each bounded context:

```
                         HTTP Request
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      infrastructure/                        │
│         Controllers, filters, Spring Security config        │
│   AirportController, RouteController, etc.                  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       application/                          │
│              One Use Case class per user action             │
│   RegisterAirportUseCase, CreateRouteUseCase, etc.          │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                         domain/                             │
│         Entities, value objects, repository interfaces      │
│   Airport, Route, Aircraft, MaintenanceRecord, etc.         │
└─────────────────────────────────────────────────────────────┘
```

Each bounded context follows this same three-layer structure. Cross-cutting concerns live in `shared`:

```
src/main/java/aisafe/
├── AisafeApplication.java
├── aircrafts/
│   ├── domain/          # Aircraft, AircraftModel, RegistrationNumber, etc.
│   ├── application/     # RegisterAircraftUseCase, ListAircraftUseCase, etc.
│   └── infrastructure/  # AircraftController, AircraftModelController
├── airports/
│   ├── domain/          # Airport, Runway, IataCode, AirportStatus, etc.
│   ├── application/     # RegisterAirportUseCase, SearchAirportUseCase, etc.
│   └── infrastructure/  # AirportController
├── routes/
│   ├── domain/          # Route, RouteHistory, RouteRepository, etc.
│   ├── application/     # CreateRouteUseCase, SearchRoutesUseCase, etc.
│   └── infrastructure/  # RouteController
├── maintenance/
│   ├── domain/          # MaintenanceRecord, MaintenancePart, MaintenanceTemplate, etc.
│   ├── application/     # CreateMaintenanceRecordUseCase, etc.
│   └── infrastructure/  # MaintenanceController
├── security/
│   ├── domain/          # User, Role, UserRepository
│   ├── application/     # AuthenticateUserUseCase, JwtService, etc.
│   └── infrastructure/  # JwtAuthenticationFilter, AuthController, SecurityConfig
└── shared/
    ├── domain/          # DomainException, DuplicateResourceException
    ├── application/     # UseCase (base interface + annotations)
    └── infrastructure/  # GlobalExceptionHandler, OpenApiConfig, Bootstrap, etc.
```

## Getting Started

```bash
./mvnw spring-boot:run
```

The application seeds sample data on first startup (see Bootstrap). Once running:

| Resource     | URL                                         |
|--------------|---------------------------------------------|
| Swagger UI   | http://localhost:8080/swagger-ui/index.html |
| OpenAPI spec | http://localhost:8080/v3/api-docs           |
| HTML docs    | http://localhost:8080/docs.html             |
| H2 console   | http://localhost:8080/h2-console            |

H2 JDBC URL: `jdbc:h2:mem:aisafedb`

### Default Credentials

| Username   | Password      | Role                   |
|------------|---------------|------------------------|
| admin      | admin123      | Admin                  |
| operator   | operator123   | Backoffice Operator    |
| atcc       | atcc123       | ATCC                   |
| technician | technician123 | Maintenance Technician |
| supervisor | supervisor123 | Maintenance Supervisor |

Authenticate via `POST /api/auth/login` to receive a JWT token, then pass it as `Authorization: Bearer <token>` on subsequent requests.

## Testing

JUnit tests are organised to mirror the same three-layer structure as the source code:

```
src/test/java/aisafe/
├── aircrafts/
│   ├── domain/          # Entity and value object rules (AircraftTest, RegistrationNumberTest, etc.)
│   ├── application/     # Use case behaviour with mocked repositories
│   └── infrastructure/  # Controller slice tests (MockMvc)
├── airports/            # (same pattern)
├── routes/              # (same pattern)
└── maintenance/         # (same pattern)
```

To run all tests:

```bash
./mvnw test
```

## Team Tooling

### Pre-commit Hook

A hook runs before every commit to validate the `docs/` folder structure - enforcing that each work package has the required README, PlantUML sources, and generated SVGs. Install it once after cloning:

```bash
# Unix / macOS
bash scripts/install-hooks.sh

# Windows
scripts/install-hooks.ps1
```

To check the docs structure at any time without committing:

```bash
python3 scripts/check-docs.py
```

### Diagram Generation

PlantUML `.puml` files live in `puml/` subfolders within each work package. To generate the corresponding `.svg` files:

```bash
# Unix / macOS (requires plantuml on PATH)
bash scripts/generate-svg.sh

# Windows
scripts/generate-svg.ps1
```

Use the `-l` flag to preview which files would be converted without actually generating them.

## Documentation

Full internal documentation - use cases, FURPS+, domain model, Postman collection, and client Q&A - is available at [`docs/README.md`](docs/README.md).
