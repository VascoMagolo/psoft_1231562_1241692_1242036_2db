package aisafe.airports.domain;

public class AirportNotFoundException extends RuntimeException {
    public AirportNotFoundException(String iataCode) {
        super("Airport with IATA code '" + iataCode + "' not found.");
    }
}
