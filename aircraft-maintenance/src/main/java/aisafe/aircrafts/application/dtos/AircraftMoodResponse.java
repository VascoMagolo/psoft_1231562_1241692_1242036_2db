package aisafe.aircrafts.application.dtos;

/** One aircraft in {@code GET /api/v2/unicorns}: its status and the mood it gives it. */
public record AircraftMoodResponse(
        String registrationNumber,
        String model,
        String status,
        String mood,
        String reason
) {
}
