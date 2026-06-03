package aisafe.airports.application.dtos;

import jakarta.validation.constraints.NotBlank;

public record AddCertificationRequest(
        @NotBlank String airportCode,
        @NotBlank String aircraftModelName
) {
}