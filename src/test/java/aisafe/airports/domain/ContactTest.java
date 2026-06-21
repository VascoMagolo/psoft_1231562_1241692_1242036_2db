package aisafe.airports.domain;

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

    @Test
    void ensureEqualContactsAreEqual() {
        Contact contact1 = new Contact(ContactType.EMAIL, "info@airport.pt", "General");
        Contact contact2 = new Contact(ContactType.EMAIL, "info@airport.pt", "General");
        assertEquals(contact1, contact2);
        assertEquals(contact1, contact1);
        assertNotEquals(contact1, null);
        assertNotEquals(contact1, "not a contact");
    }

    @Test
    void ensureDifferentContactsAreNotEqual() {
        assertNotEquals(
            new Contact(ContactType.EMAIL, "info@airport.pt", null),
            new Contact(ContactType.PHONE, "+351213500000", null)
        );
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(
            new Contact(ContactType.EMAIL, "info@airport.pt", null).hashCode(),
            new Contact(ContactType.EMAIL, "info@airport.pt", null).hashCode()
        );
    }

    @Test
    void ensureToStringContainsValues() {
        String str = new Contact(ContactType.EMAIL, "info@airport.pt", null).toString();
        assertTrue(str.contains("EMAIL"));
        assertTrue(str.contains("info@airport.pt"));
    }

    @Test
    void ensureDifferentValueSameTypeNotEqual() {
        assertNotEquals(
            new Contact(ContactType.EMAIL, "info1@airport.pt", null),
            new Contact(ContactType.EMAIL, "info2@airport.pt", null)
        );
    }

    @Test
    void ensureDifferentDescriptionSameTypeValueNotEqual() {
        assertNotEquals(
            new Contact(ContactType.EMAIL, "info@airport.pt", "Description 1"),
            new Contact(ContactType.EMAIL, "info@airport.pt", "Description 2")
        );
    }
}
