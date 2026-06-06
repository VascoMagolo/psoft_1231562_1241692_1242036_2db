package aisafe.aircrafts.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object for aircraft registration numbers. It normalizes the value to uppercase and enforces the expected `XX-XXX` format.
 */
public class RegistrationNumber {
    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{2}-[A-Z]{3}$");

    private String number;

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
