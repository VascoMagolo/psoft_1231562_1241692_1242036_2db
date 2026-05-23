package aisafe.airports.application.dtos;

import aisafe.airports.domain.ContactType;

import java.util.List;

/**
 * DTO for updating airport details.
 * @param operationalHours The new operational hours of the airport.
 * @param contacts A list of contact details to update, each with a type, value, and description.
 * @param imagePath The new image path for the airport.
 * @param services A list of services offered by the airport to update.
 * @param terminals A list of terminal names to update for the airport.
 * @param gates
 */
public record UpdateAirportDetailsRequest(
        String operationalHours,
        List<ContactRequest> contacts,
        String imagePath,
        List<String> services,
        List<String> terminals,
        List<String> gates
) {
    /**
     * Nested record representing a contact detail for the airport
     * @param type The type of contact
     * @param value The contact value
     * @param description A description of the contact detail
     */
    public record ContactRequest(ContactType type, String value, String description) {}
}
