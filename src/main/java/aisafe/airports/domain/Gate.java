package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Value object representing a gate at an airport.
 */
public class Gate {
    private final String identifier;

    public Gate(String identifier) {
        Assert.hasText(identifier, "Gate identifier cannot be empty");
        this.identifier = identifier;
    }

    public String getIdentifier() { return identifier; }
}
