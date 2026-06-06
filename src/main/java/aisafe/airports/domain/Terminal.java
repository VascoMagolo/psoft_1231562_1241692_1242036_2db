package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Value object representing a terminal within an airport.
 */
public class Terminal {
    private final String name;

    public Terminal(String name) {
        Assert.hasText(name, "Terminal name cannot be empty");
        this.name = name;
    }

    public String getName() { return name; }
}
