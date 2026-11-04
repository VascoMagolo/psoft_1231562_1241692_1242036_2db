package aisafe.flightcopies.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;

/**
 * Copy of a flight sent by flight-ops (contract C5).
 */
public record RecordFlightCopyRequest(
        @NotBlank String id,
        @NotNull OffsetDateTime departureDateTime,
        @NotNull OffsetDateTime arrivalDateTime,
        @NotBlank String status,
        @NotBlank String originIataCode,
        @NotBlank String destinationIataCode,
        @NotBlank String aircraftRegistration,
        @NotNull @PositiveOrZero Long version
) {
}
