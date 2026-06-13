package aisafe.routes.application.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRouteRequest(
        @NotBlank String originIataCode,
        @NotBlank String destinationIataCode,
        @NotNull @Min(1) Integer estimatedFlightTime,
        @NotNull @Positive Double minimumRange,
        @NotNull @Min(1) Integer minimumCapacity
) {}
