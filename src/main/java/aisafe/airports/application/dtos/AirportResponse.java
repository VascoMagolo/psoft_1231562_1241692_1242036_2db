package aisafe.airports.application.dtos;

import aisafe.airports.domain.Airport;

import java.util.List;

/**
 * DTO for transferring airport data in API responses.
 * @param id Unique identifier of the airport
 * @param iataCode IATA code of the airport
 * @param name Name of the airport
 * @param city  City where the airport is located
 * @param country Country where the airport is located
 * @param region Region where the airport is located
 * @param timezone Timezone of the airport
 * @param imagePath Path to an image representing the airport
 * @param operationalHours Operational hours of the airport
 * @param status Operational status of the airport
 * @param coordinates Geographical coordinates of the airport (latitude and longitude)
 * @param runways List of runways at the airport, each with name, length, and orientation
 * @param contacts List of contact information for the airport, each with type, value, and description
 * @param services List of services available at the airport
 * @param terminals List of terminals at the airport
 * @param gates List of gates at the airport
 */
public record AirportResponse(
        Long id,
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

    /**
     * Factory method to create an AirportResponse DTO from an Airport domain entity.
     * @param airport the Airport domain entity to convert
     * @return an AirportResponse DTO containing the data from the Airport entity
     */
    public static AirportResponse from(Airport airport) {
        return new AirportResponse(
                airport.getId(),
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
