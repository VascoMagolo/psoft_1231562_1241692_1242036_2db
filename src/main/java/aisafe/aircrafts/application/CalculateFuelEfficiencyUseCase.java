package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.FuelEfficiencyResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;

@UseCase(readOnly = true)
public class CalculateFuelEfficiencyUseCase {

    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;

    public CalculateFuelEfficiencyUseCase(AircraftRepository aircraftRepository, RouteRepository routeRepository) {
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
    }

    public FuelEfficiencyResponse execute(String registrationStr, String origin, String destination) {
        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registrationStr));

        Double fuelConsumption = aircraft.getFuelConsumptionPerDistanceUnit();
        Double fuelNeeded = null;

        if (origin != null && destination != null) {
            Route route = routeRepository.findByOriginAndDestination(new IataCode(origin), new IataCode(destination))
                    .orElseThrow(() -> new IllegalArgumentException("Route not found: " + origin + " -> " + destination));
            fuelNeeded = fuelConsumption * route.getMinimumRange();
        }

        return new FuelEfficiencyResponse(
                registrationStr,
                fuelConsumption,
                origin,
                destination,
                fuelNeeded
        );
    }
}
