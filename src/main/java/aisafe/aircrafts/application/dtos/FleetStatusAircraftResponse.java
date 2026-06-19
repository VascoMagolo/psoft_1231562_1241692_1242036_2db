package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;

/**
 * Lightweight aircraft representation within a fleet status group.
 */
public record FleetStatusAircraftResponse(
        String registrationNumber,
        String model,
        Manufacturer manufacturer) {
}
