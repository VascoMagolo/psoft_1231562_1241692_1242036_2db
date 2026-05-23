package aisafe.routes.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for RouteHistory persistence and retrieval.
 * Provides methods to audit and query the change history of routes.
 */
@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {
    /**
     * Retrieves all history entries associated with a specific route,
     * ordered by their creation time (if configured in the entity).
     *
     * @param routeId the unique identifier of the route
     * @return a list of RouteHistory entries, or an empty list if no history exists
     */
    List<RouteHistory> findAllByRouteId(Long routeId);
}