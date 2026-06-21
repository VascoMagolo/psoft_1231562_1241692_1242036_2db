package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
    }

    @Test
    void ensureValidAircraftIsCreated() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of("WiFi"));

        assertEquals(AircraftStatus.AVAILABLE, aircraft.getStatus());
        assertEquals(150, aircraft.getSeatCapacity());
        assertEquals(6000.0, aircraft.getRange());
    }

    @Test
    void ensureNullStatusThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(null, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of()));
    }

    @Test
    void ensureNullManufacturingDateThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, null, buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of()));
    }

    @Test
    void ensureNullModelThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), null, new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of()));
    }

    @Test
    void ensureNullRegistrationNumberThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), null, 150, 6000.0, List.of()));
    }

    @Test
    void ensureZeroSeatCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 0, 6000.0, List.of()));
    }

    @Test
    void ensureNegativeSeatCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), -5, 6000.0, List.of()));
    }

    @Test
    void ensureRangeExceedingModelMaxThrowsException() {
        AircraftModel model = buildModel(); // maxRange is 6150.0
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, 7000.0, List.of()));
    }

    @Test
    void ensureNullRangeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 150, null, List.of()));
    }

    @Test
    void ensureZeroRangeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), 150, 0.0, List.of()));
    }

    @Test
    void ensureChangeStatusUpdatesStatus() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of("WiFi"));

        aircraft.changeStatus(AircraftStatus.INACTIVE);

        assertEquals(AircraftStatus.INACTIVE, aircraft.getStatus());
    }

    @Test
    void ensureChangeStatusThrowsWhenNull() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of());

        assertThrows(AircraftInvalidFieldException.class, () -> aircraft.changeStatus(null));
    }

    @Test
    void ensureUpdateDetailsUpdatesAllFields() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of("WiFi"));

        AircraftModel newModel = new AircraftModel("B737", Manufacturer.BOEING, 26020.0, 5765.0, 842.0, null, 189);
        LocalDate newDate = LocalDate.of(2022, 6, 15);

        aircraft.updateDetails(newModel, newDate, 180, 5500.0, List.of("USB", "IFE"), AircraftStatus.UNDER_MAINTENANCE);

        assertEquals(newModel, aircraft.getModel());
        assertEquals(newDate, aircraft.getManufacturingDate());
        assertEquals(180, aircraft.getSeatCapacity());
        assertEquals(5500.0, aircraft.getRange());
        assertEquals(List.of("USB", "IFE"), aircraft.getFeatures());
        assertEquals(AircraftStatus.UNDER_MAINTENANCE, aircraft.getStatus());
    }

    @Test
    void ensureUpdateDetailsKeepsFieldsWhenNull() {
        AircraftModel originalModel = buildModel();
        LocalDate originalDate = LocalDate.of(2020, 1, 1);
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, originalDate,
                originalModel, new RegistrationNumber("CS-TPA"), 150, 6000.0, List.of("WiFi"));

        aircraft.updateDetails(null, null, null, null, null, null);

        assertEquals(originalModel, aircraft.getModel());
        assertEquals(originalDate, aircraft.getManufacturingDate());
        assertEquals(150, aircraft.getSeatCapacity());
        assertEquals(6000.0, aircraft.getRange());
        assertEquals(List.of("WiFi"), aircraft.getFeatures());
        assertEquals(AircraftStatus.AVAILABLE, aircraft.getStatus());
    }

    @Test
    void ensureNullSeatCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), buildModel(), new RegistrationNumber("CS-TPA"), null, 6000.0, List.of()));
    }

    @Test
    void ensureNullFeaturesDefaultsToEmptyList() {
        Aircraft aircraft = new Aircraft(
                AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                buildModel(), new RegistrationNumber("CS-TPA"), 150, 6000.0, null);

        assertNotNull(aircraft.getFeatures());
        assertTrue(aircraft.getFeatures().isEmpty());
    }
}
