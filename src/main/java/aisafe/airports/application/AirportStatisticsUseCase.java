package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.model.entities.Route;
import aisafe.routes.domain.RouteRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
public class AirportStatisticsUseCase {
    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public AirportStatisticsUseCase(AirportRepository airportRepository, RouteRepository routeRepository) {
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    public List<AirportStatisticsResponse> execute() {
        List<Airport> airports = airportRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        Map<Long, Long> routeCountById = airports.stream()
                .collect(Collectors.toMap(Airport::getId, airport ->
                        routes.stream().filter(r ->
                                (r.getOriginAirport() != null && r.getOriginAirport().getId().equals(airport.getId())) ||
                                (r.getDestinationAirport() != null && r.getDestinationAirport().getId().equals(airport.getId()))
                        ).count()
                ));

        return airports.stream()
                .map(a -> new AirportStatisticsResponse(
                        a.getIataCode().getCode(),
                        a.getName(),
                        a.getCity(),
                        a.getCountry(),
                        routeCountById.getOrDefault(a.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(AirportStatisticsResponse::routeCount).reversed())
                .toList();
    }
}
