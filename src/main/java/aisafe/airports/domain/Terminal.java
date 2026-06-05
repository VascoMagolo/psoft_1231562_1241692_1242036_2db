package aisafe.airports.domain;

/**
 * Value object representing a terminal within an airport.
 */
public class Terminal {
    private final String name;

    public Terminal(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Terminal name cannot be empty");
        this.name = name;
    }

    public String getName() { return name; }
}
