package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.FuelEfficiencyResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.shared.application.UseCase;

@UseCase
public class CalculateFuelEfficiencyUseCase {

    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;

    public CalculateFuelEfficiencyUseCase(AircraftRepository aircraftRepository, RouteRepository routeRepository) {
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
    }

    public FuelEfficiencyResponse execute(String registrationStr, Long routeId) {
        RegistrationNumber registration = new RegistrationNumber(registrationStr);
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(registration)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft not found with registration: " + registrationStr));

        Double fuelConsumption = aircraft.getFuelConsumptionPerDistanceUnit();
        Double fuelNeeded = null;

        if (routeId != null) {
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Route not found with ID: " + routeId));
            
            fuelNeeded = fuelConsumption * route.getMinimumRange();
        }

        return new FuelEfficiencyResponse(
                registrationStr,
                fuelConsumption,
                routeId,
                fuelNeeded
        );
    }
}
