package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;

public record UpdateStatusRequest(AircraftStatus status) {
}
