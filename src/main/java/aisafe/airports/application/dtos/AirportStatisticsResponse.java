package aisafe.airports.application.dtos;

/**
 * Response DTO representing airport statistics, including the number of routes originating from the airport.
 * @param iataCode the IATA code of the airport
 * @param name the name of the airport
 * @param city the city where the airport is located
 * @param country the country where the airport is located
 * @param routeCount the number of routes originating from this airport
 */
public record AirportStatisticsResponse(
        String iataCode,
        String name,
        String city,
        String country,
        long routeCount
) {}
