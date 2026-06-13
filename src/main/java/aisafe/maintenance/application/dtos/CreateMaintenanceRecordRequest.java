package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a new maintenance record.
 * @param description
 * @param startDate
 * @param expectedDuration
 * @param parts
 * @param notes
 * @param template
 * @param status
 * @param registrationNumber
 */
public record CreateMaintenanceRecordRequest(
        @NotBlank String description,
        @NotNull LocalDateTime startDate,
        @NotNull @Min(1) Integer expectedDuration,
        @NotEmpty List<String> parts,
        String notes,
        @NotBlank String template,
        @NotNull MaintenanceStatus status,
        @NotBlank String registrationNumber) {
}
