package aisafe.aircrafts.application.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload used to register a new aircraft.
 */
public record RegisterAircraftRequest(
	@NotBlank(message = "Registration number is required")
	@Pattern(regexp = "^[A-Z]{2}-[A-Z]{3}$", message = "Invalid registration format, Expected XX-XXX")
	String registrationNumber,
	@NotNull(message = "Model name is required")
	String modelName,
	@NotNull(message = "Manufacturing date is required")
	LocalDate manufacturingDate,
	@NotNull(message = "Seat capacity is required")
	@Min(value = 1, message = "Seat capacity must be greater than zero")
	Integer seatCapacity,
	@NotNull(message = "Range is required")
	@DecimalMin(value = "1.0", message = "Range must be greater than zero")
	Double range,
	@NotBlank(message = "Status is required")
	String status,
	List<String> features) {
}
