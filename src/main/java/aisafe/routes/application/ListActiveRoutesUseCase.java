package aisafe.routes.application;

import aisafe.flights.domain.FlightRepository;
import aisafe.routes.application.dtos.ActiveRouteResponse;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class ListActiveRoutesUseCase {

    private final RouteRepository routeRepository;
    private final FlightRepository flightRepository;
    private final RouteDistanceService routeDistanceService;

    public List<ActiveRouteResponse> execute(String status, String sortBy) {
        if (status != null && !status.equalsIgnoreCase("active")) {
            throw new IllegalArgumentException("Only active route listing is supported.");
        }
        String normalizedSort = sortBy == null ? "distance" : sortBy.trim().toLowerCase();
        if (!normalizedSort.equals("distance") && !normalizedSort.equals("popularity")) {
            throw new IllegalArgumentException("sortBy must be distance or popularity.");
        }

        Comparator<ActiveRouteResponse> comparator = normalizedSort.equals("popularity")
                ? Comparator.comparing(ActiveRouteResponse::popularity).reversed()
                : Comparator.comparing(ActiveRouteResponse::distanceKm);

        return routeRepository.findAllActive().stream()
                .map(this::toResponse)
                .sorted(comparator)
                .toList();
    }

    private ActiveRouteResponse toResponse(Route route) {
        return new ActiveRouteResponse(
                route.getId(),
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.isActive(),
                routeDistanceService.calculateDistanceKm(route),
                flightRepository.countByRouteId(route.getId())
        );
    }
}
