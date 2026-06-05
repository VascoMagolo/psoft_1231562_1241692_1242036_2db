package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;
import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for retrieving statistics about airports
 */
@UseCase
@Transactional(readOnly = true)
public class AirportStatisticsUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public AirportStatisticsUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Executes the use case to retrieve airport statistics.
     * @return a list of AirportStatisticsResponse DTOs containing the statistics for each airport
     */
    public List<AirportStatisticsResponse> execute() {
        List<Airport> airports = airportRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        // Precompute the route count for each airport to avoid repeated filtering
        Map<String, Long> routeCountByCode = airports.stream()
                .collect(Collectors.toMap(
                        a -> a.getIataCode().getCode(),
                        a -> {
                            String code = a.getIataCode().getCode();
                            return routes.stream().filter(r ->
                                    code.equals(r.getOrigin().getCode()) ||
                                    code.equals(r.getDestination().getCode())
                            ).count();
                        }
                ));
        // Map each airport to its statistics response and sort by route count in descending order
        return airports.stream()
                .map(a -> new AirportStatisticsResponse(
                        a.getIataCode().getCode(),
                        a.getName(),
                        a.getCity(),
                        a.getCountry(),
                        routeCountByCode.getOrDefault(a.getIataCode().getCode(), 0L)
                ))
                .sorted(Comparator.comparingLong(AirportStatisticsResponse::routeCount).reversed())
                .toList();
    }
}
