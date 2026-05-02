# FURPS+ — Non-Functional Requirements

> Sources: `ProjectAssignment.pdf` and `docs/client-questions/`.

---

## Functional

Functional requirements are captured as User Stories and documented in [`docs/use-cases/`](../../use-cases/).

Key domain constraints derived from client clarifications:

- Routes exist independently of the operational status of their airports; a closed airport does not invalidate existing routes. ([source](../../../client-questions/routes/routes-airport-status-independence.md))
- Route distance is a fixed value calculated at creation time from the origin and destination airports' coordinates. ([source](../../../client-questions/routes/routes-airport-status-independence.md))
- A route is always a direct point-to-point connection; layovers are modelled as separate routes. ([source](../../../client-questions/routes/routes-no-layovers.md))
- Aircraft state transitions (e.g., to `IN_FLIGHT`) are manual operations, to be implemented in a future iteration. ([source](../../../client-questions/aircraft/us205-state-transitions.md))
- Scheduled flight status lifecycle is planned but not yet in scope. ([source](../../../client-questions/flights/us212-scheduled-flight-states.md))
- Aircraft manufacturers are not managed as entities; they come from a fixed list defined per application version. ([source](../../../client-questions/aircraft/us101-manufacturer-details.md))
- Maintenance part inventory is managed manually by the Maintenance Supervisor; automatic deduction on use is out of scope. ([source](../../../client-questions/maintenance/us226-part-relationships.md))
- Maintenance check triggers are manual (user-initiated), not automatic or scheduled. ([source](../../../client-questions/maintenance/us222-maintenance-triggers.md))

---

## Usability

- The service layer must expose its functionality via **RESTful APIs** so that any frontend (web or mobile) can interact with it. (section 1)
- APIs must follow RESTful principles: proper use of HTTP methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`), appropriate HTTP status codes (`200`, `201`, `400`, `401`, `403`, `404`, `409`, `500`), and consistent endpoint naming conventions. (section 8)
- All endpoints must be described by an **OpenAPI specification**. (sections 3.6, 4.6)
- **HATEOAS**: resource representations must include links to related resources. (sections 3.6, 4.6)
- A **Postman collection** with sample requests and responses must be provided for all use cases. (sections 3.6, 4.6)
- Error responses must include **meaningful error messages**. (section 5)

---

## Reliability

- **Input validation** must be enforced at all API endpoints. Specific rules include: IATA airport codes must follow the 3-letter format; aircraft registration numbers must follow the standard format; date ranges and numeric values (capacity, range, etc.) must be within valid bounds. (sections 5, 8)
- **Concurrent access** must be handled correctly. Critical scenarios include: updating aircraft status, modifying route information, and creating maintenance records. (sections 4.6, 8)
- **Optimistic locking** must be used where applicable (e.g., aircraft status updates) to prevent lost updates under concurrent requests. (section 5)
- **Role-based access control** must be enforced for all endpoints. Roles: Admin, Backoffice Operator, ATCC, Maintenance Technician, Maintenance Supervisor. (section 8)
- All authenticated requests must use **JWT**. (sections 3.6, 4.6)

---

## Performance

- The system must be able to handle **thousands of aircraft profiles, hundreds of airports, numerous flight routes, and detailed maintenance histories**. (section 1)
- **Pagination** must be supported for all endpoints that return long result lists. (section 4.6)

---

## Supportability

- **Automated tests** must be provided: unit tests and a Postman collection with test scripts covering all use cases. (sections 3.6, 4.6, 5)
- The alternative route search (US216) must be designed so that algorithms can be added or replaced without changing the surrounding logic. ([source](../../../client-questions/routes/us216-alternative-routes.md))
- Use of third-party libraries is permitted but must be **justified**. (section 7)
- Regular **Git commits** with meaningful messages are mandatory; lack of regular progress negatively impacts the grade. (section 8)
