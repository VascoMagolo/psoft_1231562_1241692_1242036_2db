package aisafe.aircrafts.domain;

import java.util.Objects;

public final class ModelName {
    private final String name;

    public ModelName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new AircraftModelInvalidFieldException("Aircraft model name cannot be blank.");
        }
        this.name = name.trim();
    }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelName that = (ModelName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
