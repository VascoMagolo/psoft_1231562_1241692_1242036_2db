package aisafe.airports.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Request body for registering a new airport")
public record RegisterAirportRequest(
        @Schema(description = "3-character IATA airport code (letters or digits)", example = "LIS")
        @NotBlank(message = "IATA code is required")
        @Pattern(regexp = "[A-Za-z0-9]{3}", message = "IATA code must be exactly 3 alphanumeric characters")
        String iataCode,

        @Schema(description = "Official airport name", example = "Lisbon Humberto Delgado Airport")
        @NotBlank(message = "Airport name is required")
        String name,

        @Schema(description = "City where the airport is located", example = "Lisbon")
        @NotBlank(message = "City is required")
        String city,

        @Schema(description = "Country where the airport is located", example = "Portugal")
        @NotBlank(message = "Country is required")
        String country,

        @Schema(description = "Geographic region (optional)", example = "Southern Europe")
        String region,

        @Schema(description = "IANA timezone identifier", example = "Europe/Lisbon")
        @NotBlank(message = "Timezone is required")
        String timezone,

        @Schema(description = "Latitude in decimal degrees", example = "38.7742")
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0",   message = "Latitude must be <= 90")
        Double latitude,

        @Schema(description = "Longitude in decimal degrees", example = "-9.1342")
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
        Double longitude,

        @Schema(description = "List of runways (at least one required)")
        @NotEmpty(message = "At least one runway is required")
        @Valid
        List<RunwayRequest> runways,

        @Schema(description = "Path or URL to an airport image (optional)", example = "https://example.com/lis.jpg")
        String imagePath,

        @Schema(description = "Operational hours description (optional)", example = "24/7")
        String operationalHours,

        @Schema(description = "List of services available (optional)", example = "[\"WiFi\", \"Parking\"]")
        List<String> services,

        @Schema(description = "List of terminal names (optional)", example = "[\"Terminal 1\", \"Terminal 2\"]")
        List<String> terminals,

        @Schema(description = "List of gate identifiers (optional)", example = "[\"A1\", \"B3\"]")
        List<String> gates
) {
    @Schema(description = "Runway details")
    public record RunwayRequest(
            @Schema(description = "Runway name or identifier", example = "03/21")
            @NotBlank(message = "Runway name is required") String name,

            @Schema(description = "Runway length in meters", example = "3805")
            @NotNull(message = "Runway length is required") @Positive(message = "Runway length must be positive") Integer length,

            @Schema(description = "Runway orientation (compass bearing or direction)", example = "030/210")
            @NotBlank(message = "Runway orientation is required") String orientation
    ) {}
}
