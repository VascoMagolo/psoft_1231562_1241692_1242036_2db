package aisafe.model.valueObject;

import aisafe.exceptions.InvalidIataCodeException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.regex.Pattern;

@Embeddable
@Getter
public class IataCode {
    @Column(length = 3, nullable = false, unique = true)
    private String code;

    // regEX for iata code
    private static final Pattern IATA_PATTERN = Pattern.compile("^[A-Z0-9]{3}$");
    protected IataCode() {}

    public IataCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidIataCodeException("IATA code cannot be null or empty.");
        }
        code = code.trim().toUpperCase();
        if (!IATA_PATTERN.matcher(code).matches()){
            throw new InvalidIataCodeException("Invalid IATA code format.");
        }

        this.code = code;
    }
}
