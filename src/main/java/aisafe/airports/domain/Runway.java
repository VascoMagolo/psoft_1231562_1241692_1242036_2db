package aisafe.airports.domain;

import java.util.Objects;

public final class Runway {
    private final String name;
    private final Integer length;
    private final String orientation;

    public Runway(String name, Integer length, String orientation) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRunwayException("Runway name cannot be empty.");
        }
        if (length == null) {
            throw new InvalidRunwayException("Runway length cannot be null.");
        }
        if (orientation == null || orientation.trim().isEmpty()) {
            throw new InvalidRunwayException("Runway orientation cannot be empty.");
        }
        this.name = name;
        this.length = length;
        this.orientation = orientation;
    }

    public String getName() { return name; }
    public Integer getLength() { return length; }
    public String getOrientation() { return orientation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Runway that = (Runway) o;
        return Objects.equals(name, that.name) && Objects.equals(length, that.length) && Objects.equals(orientation, that.orientation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, length, orientation);
    }

    @Override
    public String toString() {
        return "Runway{name='" + name + "', length=" + length + ", orientation='" + orientation + "'}";
    }
}
