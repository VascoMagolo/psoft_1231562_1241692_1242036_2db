package aisafe.aircrafts.application.dtos;

public record AircraftOperationalHoursResponse(
        String registrationNumber,
        Double totalOperationalHours
) {}
