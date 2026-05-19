package aisafe.maintenance.application.dtos;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.MaintenanceStatus;

import java.time.LocalDateTime;

public record CreateMaintenanceRecordRequest(String description, LocalDateTime startDate, Integer expectedDuration, String part, String notes, String template,
                                             MaintenanceStatus status, String registrationNumber) {
}
