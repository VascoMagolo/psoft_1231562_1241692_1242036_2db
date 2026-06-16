package aisafe.flights.domain;

import aisafe.shared.domain.ResourceInUseException;

public class AircraftUnavailableException extends ResourceInUseException {
    public AircraftUnavailableException(String aircraftRegistrationNumber) {
        super("Aircraft " + aircraftRegistrationNumber + " is unavailable in the requested timeframe.");
    }
}
