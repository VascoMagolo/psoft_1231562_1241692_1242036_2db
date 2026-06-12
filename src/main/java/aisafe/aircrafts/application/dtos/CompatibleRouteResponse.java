package aisafe.aircrafts.application.dtos;

import aisafe.airports.domain.IataCode;

/**
 * Data transfer object representing the response for compatible routes of an aircraft.
 */
public record CompatibleRouteResponse (IataCode origin, IataCode destination, Integer estimatedFlightTime, Double minimumRange, Integer minimumCapacity){}
