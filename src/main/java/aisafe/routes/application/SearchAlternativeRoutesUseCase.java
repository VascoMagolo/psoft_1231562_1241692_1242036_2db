package aisafe.routes.application;

import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.routes.application.dtos.AlternativeRouteResponse;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class SearchAlternativeRoutesUseCase {

    private final AirportRepository airportRepository;
    private final NetworkGraphService networkGraphService;

    public List<AlternativeRouteResponse> execute(String origin, String destination) {
        String normalizedOrigin = origin.trim().toUpperCase();
        String normalizedDestination = destination.trim().toUpperCase();

        if (normalizedOrigin.equals(normalizedDestination)) {
            throw new IllegalArgumentException("Origin and destination cannot be the same.");
        }
        if (!airportRepository.existsByIataCodeCode(normalizedOrigin)) {
            throw new AirportNotFoundException(normalizedOrigin);
        }
        if (!airportRepository.existsByIataCodeCode(normalizedDestination)) {
            throw new AirportNotFoundException(normalizedDestination);
        }

        return networkGraphService.findAlternativePaths(normalizedOrigin, normalizedDestination).stream()
                .map(path -> new AlternativeRouteResponse(path, path.size() - 2))
                .toList();
    }
}
