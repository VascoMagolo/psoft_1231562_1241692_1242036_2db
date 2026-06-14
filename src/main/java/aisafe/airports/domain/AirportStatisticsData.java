package aisafe.airports.domain;

public record AirportStatisticsData(String iataCode, String name, String city, String country, long routeCount) {}
