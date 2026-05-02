package aisafe.airports.application.dtos;

import aisafe.airports.domain.AirportStatus;

public record UpdateAirportStatusRequest(AirportStatus status) {}
