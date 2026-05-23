# AISafe - Documentation

AISafe exposes a REST API (Spring Boot) for managing aircraft, airports, flight routes, and maintenance records.

---

## Navigation

| Resource                                                    | Description                                                  |
| ----------------------------------------------------------- | ------------------------------------------------------------ |
| [Team Members & Task Distribution](TeamMembersAndTasks.md)  | Who owns each User Story and work package                    |
| [System Documentation](system-documentation)                | Architecture, domain model, and use-case artefacts           |
| [Global Artefacts](system-documentation/global-artifacts)   | Glossary, FURPS+, and domain model diagrams                  |
| [Use Cases](system-documentation/use-cases)                 | Per-WP folders with sequence diagrams (SD/SSD)               |
| [Client Q&A](client-questions)                              | Clarifications obtained from the client, organised by domain |
| [Postman Collection](postman/psoft.postman_collection.json) | Ready-to-import collection covering all API endpoints        |

---

## Work Packages

| WP    | Domain                                            | Owner           |
| ----- | ------------------------------------------------- | --------------- |
| WP#1A | Aircraft Models & Aircraft                        | Vasco Magolo    |
| WP#2A | Airports (core)                                   | Diogo Nogueira  |
| WP#2B | Airports (extended - facilities, stats, grouping) | Diogo Nogueira  |
| WP#3A | Flight Routes                                     | Pedro Guilherme |
| WP#4A | Maintenance Records                               | Shared          |

---

## Running the API

```bash
./mvnw spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- Custom HTML Docs: `http://localhost:8080/docs.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:aisafedb`)
