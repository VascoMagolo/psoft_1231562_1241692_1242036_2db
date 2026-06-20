package aisafe.airports.application.dtos;

public record SearchAirportRequest(String name, String city, String country, int pageNumber, int pageSize) {}
