package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {
    long count();
    Route save(Route route);
    Optional<Route> findById(Long id);
    boolean existsById(Long id);
    void delete(Route route);
    List<Route> findAll();
    Page<Route> findAll(Pageable pageable);
    Page<Route> findByOrigin(IataCode origin, Pageable pageable);
    Page<Route> findByDestination(IataCode destination, Pageable pageable);
    Page<Route> findByOriginAndDestination(IataCode origin, IataCode destination, Pageable pageable);
    boolean existsByOriginAndDestination(IataCode origin, IataCode destination);
    List<Route> findByOriginOrDestination(IataCode origin, IataCode destination);
}
