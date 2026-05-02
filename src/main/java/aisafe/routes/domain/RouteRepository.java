package aisafe.routes.domain;

import aisafe.model.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOriginAirportIataCodeCodeOrDestinationAirportIataCodeCode(
            String originIata, String destinationIata);
}
