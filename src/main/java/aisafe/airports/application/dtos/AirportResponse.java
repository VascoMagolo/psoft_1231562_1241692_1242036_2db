package aisafe.airports.application.dtos;

import aisafe.airports.domain.Airport;

import java.util.List;

/**
 * DTO for transferring airport data in API responses.
 */
public record AirportResponse(
        String iataCode,
        String name,
        String city,
        String country,
        String region,
        String timezone,
        String imagePath,
        String operationalHours,
        String status,
        CoordinatesRecord coordinates,
        List<RunwayRecord> runways,
        List<ContactRecord> contacts,
        List<String> services,
        List<String> terminals,
        List<String> gates
) {
    public record CoordinatesRecord(Double latitude, Double longitude) {}
    public record RunwayRecord(String name, Integer length, String orientation) {}
    public record ContactRecord(String type, String value, String description) {}

    public static AirportResponse from(Airport airport) {
        return new AirportResponse(
                airport.getIataCode().getCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getRegion(),
                airport.getTimezone(),
                airport.getImagePath(),
                airport.getOperationalHours(),
                airport.getStatus().name(),
                new CoordinatesRecord(
                        airport.getCoordinates().getLatitude(),
                        airport.getCoordinates().getLongitude()
                ),
                airport.getRunways().stream()
                        .map(r -> new RunwayRecord(r.getName(), r.getLength(), r.getOrientation()))
                        .toList(),
                airport.getContacts().stream()
                        .map(c -> new ContactRecord(c.getType().name(), c.getValue(), c.getDescription()))
                        .toList(),
                airport.getServices().stream().map(s -> s.getDescription()).toList(),
                airport.getTerminals().stream().map(t -> t.getName()).toList(),
                airport.getGates().stream().map(g -> g.getIdentifier()).toList()
        );
    }
}
