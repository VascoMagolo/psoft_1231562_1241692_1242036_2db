package aisafe.routes.application;

import aisafe.UseCase;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case responsible for listing all routes originating from a specific airport.
 */
@UseCase
@RequiredArgsConstructor
public class ListRoutesFromAirportUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;

    /**
     * Retrieves a page of routes associated with the provided airport IATA code.
     *
     * @param iataCode the IATA code of the origin airport
     * @param pageable the pagination information
     * @return a page of routes originating from the specified airport
     */
    public Page<Route> execute(String iataCode, Pageable pageable) {
        IataCode origin = new IataCode(iataCode);
        String originCode = iataCode.trim().toUpperCase();

        if (!airportRepository.existsByIataCodeCode(originCode)) {
            throw new AirportNotFoundException(originCode);
        }

        return routeRepository.findByOrigin(origin, pageable);
    }
}