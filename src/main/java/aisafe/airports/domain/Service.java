package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Represents a service offered at an airport.
 */
public class Service {
    private final String description;

    public Service(String description) {
        Assert.hasText(description, "Service description cannot be empty");
        this.description = description;
    }

    public String getDescription() { return description; }
}
