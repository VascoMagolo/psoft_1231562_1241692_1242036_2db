package aisafe.airports.application.dtos;

public record AirportStatisticsResponse(
        String iataCode,
        String name,
        String city,
        String country,
        long routeCount
) {}
