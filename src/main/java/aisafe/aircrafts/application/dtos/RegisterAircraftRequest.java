package aisafe.aircrafts.application.dtos;

import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload used to register a new aircraft.
 */
public record RegisterAircraftRequest(
	@NotBlank(message = "Registration number is required")
	@Pattern(regexp = "^[A-Z0-9-]{3,10}$", message = "Invalid registration format")
	String registrationNumber,
	@NotNull(message = "Model ID is required")
	Long modelId,
	@NotNull(message = "Manufacturing date is required")
	LocalDate manufacturingDate,
	@NotNull(message = "Seat capacity is required")
	@Min(value = 1, message = "Seat capacity must be greater than zero")
	Integer seatCapacity,
	@NotNull(message = "Status is required")
	String status,
	List<String> features) {
}
