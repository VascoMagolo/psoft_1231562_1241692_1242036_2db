# US216 -- Search Alternative Routes

## User Story

> As an **ATCC**, I want to search for alternative routes between two airports.

## Acceptance Criteria

- The request must specify an origin and destination airport (IATA codes).
- The system must return viable alternative paths (sequence of connected airports) that do not consist solely of the direct route.
- On success, the system returns HTTP 200 with the list of alternative paths.

## Pre-conditions

- The actor is authenticated as an ATCC.
- Both origin and destination airports must exist.

## Post-conditions

- None (Read-only operation).

## Main Success Scenario

1. The actor sends `GET /api/routes/alternatives?origin={A}&destination={B}`.
2. The system retrieves the active network state (all active routes).
3. The system builds a graph where airports are nodes and routes are edges.
4. The system applies a pathfinding algorithm (e.g., K-Shortest Paths) to find alternatives.
5. The system returns HTTP 200 OK with the found paths.

## Alternative / Exception Flows

| Step | Condition                         | System Response |
| ---- | --------------------------------- | --------------- |
| 1    | Origin equals destination         | HTTP 400 Bad Request |
| 2    | One or both airports not found    | HTTP 404 Not Found |
| 4    | No alternative paths exist        | HTTP 200 OK (Empty List) |

## Design Justification

- A dedicated `NetworkGraphService` was introduced. Graph traversal algorithms (like Yen's or Dijkstra's) are computationally heavy and logically complex. Separating this from the typical CRUD Use Cases ensures the code remains clean, testable, and highly cohesive.
- The use case fetches all active routes to build the network state dynamically, guaranteeing that the pathfinding always considers real-time operational availability.

## Sequence Diagrams

- [System Sequence Diagram](svg/ssd_us216.svg)
- [Sequence Diagram](svg/sd_us216.svg)