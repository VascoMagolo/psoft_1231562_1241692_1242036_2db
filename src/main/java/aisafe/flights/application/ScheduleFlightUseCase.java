package aisafe.flights.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.application.dtos.FlightResponse;
import aisafe.flights.application.dtos.ScheduleFlightRequest;
import aisafe.flights.domain.AircraftIncompatibilityException;
import aisafe.flights.domain.AircraftUnavailableException;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.RouteDistanceService;
import aisafe.flights.domain.FlightStatus;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteNotFoundException;
import aisafe.routes.domain.RouteRepository;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.DomainException;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ScheduleFlightUseCase {

    private final ScheduledFlightRepository scheduledFlightRepository;
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;
    private final RouteDistanceService routeDistanceService;

    public FlightResponse execute(ScheduleFlightRequest request) {
        String aircraftId = request.aircraftId().trim().toUpperCase();
        String origin = request.originIataCode().trim().toUpperCase();
        String destination = request.destinationIataCode().trim().toUpperCase();

        Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                .orElseThrow(() -> new RouteNotFoundException(origin + " -> " + destination));
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(aircraftId))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found: " + aircraftId));

        if (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE || aircraft.getStatus() == AircraftStatus.INACTIVE) {
            throw new AircraftUnavailableException(aircraftId);
        }
        if (aircraft.getSeatCapacity() < route.getMinimumCapacity()) {
            throw new AircraftIncompatibilityException("Aircraft capacity (" + aircraft.getSeatCapacity() + ") is less than the route minimum capacity (" + route.getMinimumCapacity() + ").");
        }
        if (aircraft.getRange() < route.getMinimumRange()) {
            throw new AircraftIncompatibilityException("Aircraft range (" + aircraft.getRange() + ") is less than the route minimum range (" + route.getMinimumRange() + ").");
        }
        if (routeDistanceService.calculateDistanceKm(route) > aircraft.getRange()) {
            throw new AircraftIncompatibilityException("Route distance exceeds aircraft maximum range.");
        }
        RegistrationNumber regNum = new RegistrationNumber(aircraftId);
        if (scheduledFlightRepository.existsByOverlappingSchedule(regNum, request.departureDateTime(), request.arrivalDateTime())) {
            throw new AircraftUnavailableException(aircraftId);
        }

        ScheduledFlight scheduledFlight = new ScheduledFlight(
                request.departureDateTime(),
                request.arrivalDateTime(),
                FlightStatus.SCHEDULED,
                new IataCode(origin),
                new IataCode(destination),
                regNum
        );
        ScheduledFlight saved = scheduledFlightRepository.save(scheduledFlight);
        return FlightResponse.from(saved);
    }
}
