package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.CreateRouteRequest;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class CreateRouteUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    @Transactional
    public Route execute(CreateRouteRequest request) {
        IataCode origin = new IataCode(request.originIataCode());
        IataCode destination = new IataCode(request.destinationIataCode());
        String originCode = request.originIataCode().trim().toUpperCase();
        String destinationCode = request.destinationIataCode().trim().toUpperCase();

        if (!airportRepository.existsByIataCodeCode(originCode)) {
            throw new AirportNotFoundException(originCode);
        }
        if (!airportRepository.existsByIataCodeCode(destinationCode)) {
            throw new AirportNotFoundException(destinationCode);
        }
        if (routeRepository.existsByOriginAndDestination(origin, destination)) {
            throw new IllegalArgumentException("Route already exists between origin and destination.");
        }

        Route route = new Route(
                originCode,
                destinationCode,
                request.estimatedFlightTime(),
                request.minimumRange(),
                request.minimumCapacity()
        );

        return routeRepository.save(route);
    }
}
