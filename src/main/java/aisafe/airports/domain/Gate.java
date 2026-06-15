package aisafe.airports.domain;

import java.util.Objects;

public final class Gate {
    private final String identifier;

    public Gate(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new InvalidGateException("Gate identifier cannot be empty.");
        }
        this.identifier = identifier;
    }

    public String getIdentifier() { return identifier; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gate that = (Gate) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return "Gate{identifier='" + identifier + "'}";
    }
}
