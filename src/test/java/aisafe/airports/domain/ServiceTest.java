package aisafe.airports.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    @Test
    void ensureValidServiceIsCreated() {
        Service service = new Service("Fuel refueling");
        assertEquals("Fuel refueling", service.getDescription());
    }

    @Test
    void ensureNullDescriptionThrowsException() {
        assertThrows(InvalidServiceException.class, () -> new Service(null));
    }

    @Test
    void ensureEmptyDescriptionThrowsException() {
        assertThrows(InvalidServiceException.class, () -> new Service("  "));
    }

    @Test
    void ensureEqualServicesAreEqual() {
        assertEquals(new Service("Fuel refueling"), new Service("Fuel refueling"));
    }

    @Test
    void ensureDifferentServicesAreNotEqual() {
        assertNotEquals(new Service("Fuel refueling"), new Service("Baggage handling"));
    }

    @Test
    void ensureHashCodeIsConsistentWithEquals() {
        assertEquals(new Service("Fuel refueling").hashCode(), new Service("Fuel refueling").hashCode());
    }

    @Test
    void ensureToStringContainsDescription() {
        assertTrue(new Service("Fuel refueling").toString().contains("Fuel refueling"));
    }

    @Test
    void ensureEqualsSameReference() {
        Service service = new Service("Fuel refueling");
        assertEquals(service, service);
    }

    @Test
    void ensureEqualsNull() {
        Service service = new Service("Fuel refueling");
        assertFalse(service.equals(null));
    }

    @Test
    void ensureEqualsDifferentClass() {
        Service service = new Service("Fuel refueling");
        assertFalse(service.equals("Fuel refueling"));
    }
}
