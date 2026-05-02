package aisafe.airports.application.dtos;

import aisafe.model.enums.ContactType;

import java.util.List;

public record UpdateAirportDetailsRequest(
        String operationalHours,
        List<ContactRequest> contacts,
        String imagePath,
        List<String> services,
        List<String> terminals,
        List<String> gates
) {
    public record ContactRequest(ContactType type, String value, String description) {}
}
