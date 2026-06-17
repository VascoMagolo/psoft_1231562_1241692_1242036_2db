package aisafe.routes.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.routes.application.dtos.ExportedFile;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
@RequiredArgsConstructor
public class ExportRouteNetworkUseCase {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final List<RouteNetworkSerializer> serializers;

    public ExportedFile execute(String format) {
        RouteNetworkSerializer serializer = serializers.stream()
                .filter(s -> s.supports(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));

        List<Route> activeRoutes = routeRepository.findAllActive();
        
        Set<IataCode> airportCodes = activeRoutes.stream()
                .flatMap(r -> List.of(r.getOrigin(), r.getDestination()).stream())
                .collect(Collectors.toSet());

        Map<String, Airport> airports = airportCodes.stream()
                .map(code -> airportRepository.findByIataCode(code)
                        .orElseThrow(() -> new IllegalStateException("Airport not found: " + code.getCode())))
                .collect(Collectors.toMap(a -> a.getIataCode().getCode(), a -> a));

        return serializer.serialize(activeRoutes, airports);
    }
}
