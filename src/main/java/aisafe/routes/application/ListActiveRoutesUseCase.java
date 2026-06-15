package aisafe.routes.application;

import aisafe.routes.application.dtos.ActiveRouteResponse;
import aisafe.routes.domain.InvalidSortParameterException;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.InvalidListingCriteriaException;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@UseCase(readOnly = true)
@RequiredArgsConstructor
public class ListActiveRoutesUseCase {

    private final RouteRepository routeRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;
    private final RouteDistanceService routeDistanceService;

    public List<ActiveRouteResponse> execute(String status, String sortBy) {
        if (status != null && !status.equalsIgnoreCase("active")) {
            throw new InvalidListingCriteriaException("Only active route listing is supported.");
        }
        String normalizedSort = sortBy == null ? "distance" : sortBy.trim().toLowerCase();
        if (!normalizedSort.equals("distance") && !normalizedSort.equals("popularity")) {
            throw new InvalidSortParameterException("sortBy must be distance or popularity.");
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
                route.getOrigin().getCode(),
                route.getDestination().getCode(),
                route.getEstimatedFlightTime(),
                route.getMinimumRange(),
                route.getMinimumCapacity(),
                route.getStatus(),
                routeDistanceService.calculateDistanceKm(route),
                scheduledFlightRepository.countByRoute(route.getOrigin(), route.getDestination())
        );
    }
}
