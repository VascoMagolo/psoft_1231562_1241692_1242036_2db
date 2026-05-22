package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.Manufacturer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload used to register a new aircraft model.
 */

public record RegisterAircraftModelRequest(
        @NotBlank String modelName,
        @NotNull Manufacturer manufacturer,
        @NotNull @DecimalMin("1.0") Double maxRange,
        @NotNull @DecimalMin("1.0") Double fuelCapacity,
        @NotNull @DecimalMin("1.0") Double cruisingSpeed,
        @NotNull @Min(1) Integer maximumSeatingCapacity,
        @NotBlank String imagePath
) {}