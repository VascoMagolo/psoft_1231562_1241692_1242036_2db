package aisafe.airports.domain;

import aisafe.airports.domain.InvalidContactException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {

    @Test
    void ensureValidEmailContactIsCreated() {
        Contact contact = new Contact(ContactType.EMAIL, "info@airport.pt", "General inquiries");
        assertEquals(ContactType.EMAIL, contact.getType());
        assertEquals("info@airport.pt", contact.getValue());
    }

    @Test
    void ensureValidPhoneContactIsCreated() {
        Contact contact = new Contact(ContactType.PHONE, "+351213500000", null);
        assertEquals(ContactType.PHONE, contact.getType());
        assertEquals("+351213500000", contact.getValue());
    }

    @Test
    void ensureValidFaxContactIsCreated() {
        assertDoesNotThrow(() -> new Contact(ContactType.FAX, "351213500000", null));
    }

    @Test
    void ensureOtherTypeAcceptsAnyNonEmptyValue() {
        assertDoesNotThrow(() -> new Contact(ContactType.OTHER, "Some custom info", null));
    }

    @Test
    void ensureNullTypeThrowsException() {
        assertThrows(InvalidContactException.class, () -> new Contact(null, "info@airport.pt", null));
    }

    @Test
    void ensureNullValueThrowsException() {
        assertThrows(InvalidContactException.class, () -> new Contact(ContactType.EMAIL, null, null));
    }

    @Test
    void ensureEmptyValueThrowsException() {
        assertThrows(InvalidContactException.class, () -> new Contact(ContactType.OTHER, "  ", null));
    }

    @Test
    void ensureInvalidEmailFormatThrowsException() {
        assertThrows(InvalidContactException.class, () -> new Contact(ContactType.EMAIL, "not-an-email", null));
    }

    @Test
    void ensureInvalidPhoneFormatThrowsException() {
        assertThrows(InvalidContactException.class, () -> new Contact(ContactType.PHONE, "123", null));
    }

    @Test
    void ensureDescriptionIsTrimmed() {
        Contact contact = new Contact(ContactType.OTHER, "value", "  trimmed  ");
        assertEquals("trimmed", contact.getDescription());
    }
}
