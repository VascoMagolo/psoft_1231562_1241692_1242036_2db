package aisafe.airports.domain;

import java.util.Objects;

public final class Terminal {
    private final String name;

    public Terminal(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidTerminalException("Terminal name cannot be empty.");
        }
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terminal that = (Terminal) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Terminal{name='" + name + "'}";
    }
}
