package aisafe.airports.domain;

import aisafe.shared.domain.DomainException;

public class AirportPhotoNotFoundException extends DomainException {
    public AirportPhotoNotFoundException(String iataCode) {
        super("Airport '" + iataCode + "' has no photo.");
    }
}
