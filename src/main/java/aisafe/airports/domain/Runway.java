package aisafe.airports.domain;

import org.springframework.util.Assert;

/**
 * Represents a runway at an airport
 */
public class Runway {
    private final String name;
    private final Integer length;
    private final String orientation;

    public Runway(String name, Integer length, String orientation) {
        Assert.hasText(name, "Runway name cannot be empty");
        Assert.notNull(length, "Runway length cannot be null");
        Assert.hasText(orientation, "Runway orientation cannot be empty");
        this.length = length;
        this.name = name;
        this.orientation = orientation;
    }

    public String getName() { return name; }
    public Integer getLength() { return length; }
    public String getOrientation() { return orientation; }
}
