package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOrigin(IataCode origin);
    List<Route> findByDestination(IataCode destination);
    List<Route> findByOriginAndDestination(IataCode origin, IataCode destination);
    boolean existsByOriginAndDestination(IataCode origin, IataCode destination);
    List<Route> findByOriginOrDestination(IataCode origin, IataCode destination);
}
