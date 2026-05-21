package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.dtos.CreateRouteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class CreateRouteUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    @Transactional
    public Route execute(CreateRouteRequest request) {

        if (!airportRepository.existsByIataCode(request.getOriginIataCode())) {
            throw new AirportNotFoundException(request.getOriginIataCode());
        }
        if (!airportRepository.existsByIataCode(request.getDestinationIataCode())) {
            throw new AirportNotFoundException(request.getDestinationIataCode());
        }

        Route route = new Route(
                request.getOriginIataCode(),
                request.getDestinationIataCode(),
                request.getEstimatedFlightTime(),
                request.getMinimumRange(),
                request.getMinimumCapacity()
        );

        return routeRepository.save(route);
    }
}