package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureValidAircraftIsCreated() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, List.of("WiFi"));

        assertEquals(AircraftStatus.AVAILABLE, aircraft.getStatus());
        assertEquals(150, aircraft.getSeatCapacity());
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(null, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 150, List.of()));
    }

    @Test
    void ensureNullManufacturingDateThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, null, buildModel(), new RegistrationNumber("CS-TPA"), 150, List.of()));
    }

    @Test
    void ensureNullModelThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), null, new RegistrationNumber("CS-TPA"), 150, List.of()));
    }

    @Test
    void ensureNullRegistrationNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), null, 150, List.of()));
    }

    @Test
    void ensureZeroSeatCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 0, List.of()));
    }

    @Test
    void ensureNegativeSeatCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), -5, List.of()));
    }
}
