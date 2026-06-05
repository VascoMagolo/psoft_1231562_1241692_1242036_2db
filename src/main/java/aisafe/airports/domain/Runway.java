package aisafe.airports.domain;

/**
 * Represents a runway at an airport
 */
public class Runway {
    private final String name;
    private final Integer length;
    private final String orientation;

    public Runway(String name, Integer length, String orientation) {
        if (name == null || orientation == null || name.trim().isEmpty() || orientation.trim().isEmpty() || length == null) {
            throw new IllegalArgumentException("Name, length or orientation cannot be null");
        }
        this.length = length;
        this.name = name;
        this.orientation = orientation;
    }

    public String getName() { return name; }
    public Integer getLength() { return length; }
    public String getOrientation() { return orientation; }
}
