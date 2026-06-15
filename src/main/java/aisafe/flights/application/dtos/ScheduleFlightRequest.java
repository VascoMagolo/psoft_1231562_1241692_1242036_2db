package aisafe.flights.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduleFlightRequest(
        @NotBlank String aircraftId,
        @NotBlank String originIataCode,
        @NotBlank String destinationIataCode,
        @NotNull OffsetDateTime departureDateTime,
        @NotNull OffsetDateTime arrivalDateTime
) {}
