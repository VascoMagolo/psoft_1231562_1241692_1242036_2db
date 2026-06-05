package aisafe.airports.domain;

/**
 * Value object representing a gate at an airport.
 */
public class Gate {
    private final String identifier;

    public Gate(String identifier) {
        if (identifier == null || identifier.trim().isEmpty())
            throw new IllegalArgumentException("Gate identifier cannot be empty");
        this.identifier = identifier;
    }

    public String getIdentifier() { return identifier; }
}
