package aisafe.airports.application.dtos;

import java.util.List;

public record RegisterAirportRequest(
        String iataCode,
        String name,
        String city,
        String country,
        String region,
        String timezone,
        Double latitude,
        Double longitude,
        List<RunwayRequest> runways,
        // US207: optional enhanced fields
        String imagePath,
        String operationalHours,
        List<String> services,
        List<String> terminals,
        List<String> gates
) {
    public record RunwayRequest(String name, Integer length, String orientation) {}
}
