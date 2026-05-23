package aisafe.airports.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Embeddable class representing a contact method for an airport
 */
@Embeddable
@Getter
public class Contact {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType type;
    @Column(name = "contact_value", nullable = false)
    private String value;
    private String description;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

    protected Contact() {}

    public Contact(ContactType type, String value, String description) {
        if (type == null) {
            throw new InvalidContactException("Contact type cannot be null.");
        }
        if (value == null) {
            throw new InvalidContactException("Contact value cannot be null.");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new InvalidContactException("Contact value cannot be empty.");
        }
        switch (type) {
            case PHONE, FAX:
                if (!PHONE_PATTERN.matcher(value).matches()) {
                    throw new InvalidContactException("Invalid phone number format");
                }
                break;
            case EMAIL:
                if (!EMAIL_PATTERN.matcher(value).matches()) {
                    throw new InvalidContactException("Invalid email");
                }
                break;
            case OTHER:
                break;
        }
        this.type = type;
        this.description = (description != null) ? description.trim() : null;
        this.value = value;
    }
}
