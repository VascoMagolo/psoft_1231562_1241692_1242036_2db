package aisafe.routes.application;

import aisafe.routes.application.dtos.TotalDistanceResponse;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CalculateTotalNetworkDistanceUseCase {

    private final RouteRepository routeRepository;
    private final RouteDistanceService routeDistanceService;

    public TotalDistanceResponse execute() {
        double totalDistance = routeRepository.findAllActive().stream()
                .mapToDouble(routeDistanceService::calculateDistanceKm)
                .sum();
        return new TotalDistanceResponse(Math.round(totalDistance * 100.0) / 100.0, "km");
    }
}
