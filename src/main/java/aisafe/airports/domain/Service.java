package aisafe.airports.domain;

/**
 * Represents a service offered at an airport.
 */
public class Service {
    private final String description;

    public Service(String description) {
        if (description == null || description.trim().isEmpty())
            throw new IllegalArgumentException("Service description cannot be empty");
        this.description = description;
    }

    public String getDescription() { return description; }
}
