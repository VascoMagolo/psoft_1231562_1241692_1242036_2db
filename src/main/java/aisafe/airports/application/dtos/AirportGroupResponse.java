package aisafe.airports.application.dtos;

import aisafe.airports.domain.Airport;

import java.util.List;

public record AirportGroupResponse(String group, List<Airport> airports) {}
