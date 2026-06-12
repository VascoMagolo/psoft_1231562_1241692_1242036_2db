package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    long count();
    Route save(Route route);
    Optional<Route> findByOriginAndDestination(IataCode origin, IataCode destination);
    void delete(Route route);
    List<Route> findAll();
    PaginatedResult<Route> findAll(int pageNumber, int pageSize);
    PaginatedResult<Route> findByOrigin(IataCode origin, int pageNumber, int pageSize);
    PaginatedResult<Route> findByDestination(IataCode destination, int pageNumber, int pageSize);
    PaginatedResult<Route> findByOriginAndDestination(IataCode origin, IataCode destination, int pageNumber, int pageSize);
    boolean existsByOriginAndDestination(IataCode origin, IataCode destination);
    List<Route> findByOriginOrDestination(IataCode origin, IataCode destination);
    List<Route> findCompatibleRoutes(Double range, Integer capacity);
}
