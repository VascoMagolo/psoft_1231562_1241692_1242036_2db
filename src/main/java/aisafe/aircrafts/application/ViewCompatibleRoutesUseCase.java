package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.CompatibleRouteResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Views which routes are compatible with a specific aircraft based on its range and capacity
 */

@UseCase(readOnly = true)
@Transactional(readOnly = true)
public class ViewCompatibleRoutesUseCase {
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;

    public ViewCompatibleRoutesUseCase(RouteRepository routeRepository, AircraftRepository aircraftRepository) {
        this.routeRepository = routeRepository;
        this.aircraftRepository = aircraftRepository;
    }

    /**
     * Finds all routes that are compatible with the specified aircraft's range and seat capacity.
     * @param registrationNumber The registration number of the aircraft for which to find compatible routes.
     * @return A list of compatible routes, each containing the origin, destination, estimated flight time, minimum range, and minimum capacity.
     */
    public List<CompatibleRouteResponse> execute(RegistrationNumber registrationNumber) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration " + registrationNumber + " not found."));

        return routeRepository.findCompatibleRoutes(aircraft.getRange(), aircraft.getSeatCapacity())
                .stream()
                .map(route -> new CompatibleRouteResponse(
                        route.getOrigin().getCode(),
                        route.getDestination().getCode(),
                        route.getEstimatedFlightTime(),
                        route.getMinimumRange(),
                        route.getMinimumCapacity()
                ))
                .collect(Collectors.toList());
    }
}
