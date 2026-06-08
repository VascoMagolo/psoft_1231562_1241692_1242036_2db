package aisafe.flights.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.flights.domain.AircraftUnavailableException;
import aisafe.flights.domain.Flight;
import aisafe.flights.domain.FlightRepository;
import aisafe.routes.application.RouteDistanceService;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class  ScheduleFlightUseCase {

    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;
    private final RouteDistanceService routeDistanceService;

    public Flight execute(ScheduleFlightRequest request) {
        String aircraftId = request.aircraftId().trim().toUpperCase();
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new RouteNotFoundException(String.valueOf(request.routeId())));
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(aircraftId))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found: " + aircraftId));

        if (routeDistanceService.calculateDistanceKm(route) > aircraft.getModel().getMaxRange()) {
            throw new DomainException("Route distance exceeds aircraft maximum range.");
        }
        if (flightRepository.hasOverlappingFlights(aircraftId, request.departureDateTime(), request.arrivalDateTime())) {
            throw new AircraftUnavailableException(aircraftId);
        }

        return flightRepository.save(new Flight(
                aircraftId,
                route.getId(),
                request.departureDateTime(),
                request.arrivalDateTime()
        ));
    }
}
