package aisafe.aircrafts.application.dtos;

import aisafe.model.enums.AircraftStatus;

public record UpdateStatusRequest(AircraftStatus status) {
}
