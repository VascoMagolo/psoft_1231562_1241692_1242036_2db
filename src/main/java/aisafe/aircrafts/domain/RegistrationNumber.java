package aisafe.aircrafts.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
@Embeddable
public class RegistrationNumber {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z]{3}$");

    @Column(name = "registration_number", unique = true, nullable = false, updatable = false)
    private String number;

    protected RegistrationNumber() {
    }

    @JsonCreator
    public RegistrationNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Aircraft registration number cannot be empty.");
        }

        String upperCaseNumber = number.trim().toUpperCase();

        if (!PATTERN.matcher(upperCaseNumber).matches()) {
            throw new IllegalArgumentException("Invalid registration number format. Expected format: XX-XXX (e.g., CS-TPA).");
        }

        this.number = upperCaseNumber;
    }

    @JsonValue
    public String getNumber() {
        return number;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationNumber that = (RegistrationNumber) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return number;
    }
}
