package aisafe.airports.domain;

import java.util.Objects;

public final class Service {
    private final String description;

    public Service(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidServiceException("Service description cannot be empty.");
        }
        this.description = description;
    }

    public String getDescription() { return description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service that = (Service) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }

    @Override
    public String toString() {
        return "Service{description='" + description + "'}";
    }
}
