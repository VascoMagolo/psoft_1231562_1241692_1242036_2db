package aisafe.maintenance.application.dtos;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
 * @param components
 */
public record CreateMaintenanceRecordRequest(
        @NotBlank String description,
        @NotNull LocalDateTime startDate,
        @NotNull @Min(1) Integer expectedDuration,
        @NotEmpty List<String> parts,
        String notes,
        @NotBlank String template,
        @NotNull MaintenanceStatus status,
        @NotBlank String registrationNumber,
        @NotEmpty
        @ArraySchema(schema = @Schema(implementation = MaintenanceComponent.class),
                arraySchema = @Schema(description = "Components covered by this maintenance event",
                        requiredMode = Schema.RequiredMode.REQUIRED))
        Set<MaintenanceComponent> components,
        @NotNull @DecimalMin("0.00") BigDecimal cost) {
}
