package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    @Test
    void ensureValidModelIsCreated() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        assertEquals("A320", model.getModelName());
        assertEquals(Manufacturer.AIRBUS, model.getManufacturer());
        assertEquals(180, model.getMaximumSeatingCapacity());
    }

    @Test
    void ensureBlankModelNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("  ", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180));
    }

    @Test
    void ensureNullManufacturerThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", null, 26730.0, 6150.0, 833.0, "a320.jpg", 180));
    }

    @Test
    void ensureZeroMaxRangeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 0.0, 833.0, "a320.jpg", 180));
    }

    @Test
    void ensureNegativeMaxRangeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, -100.0, 833.0, "a320.jpg", 180));
    }

    @Test
    void ensureZeroFuelCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 0.0, 6150.0, 833.0, "a320.jpg", 180));
    }

    @Test
    void ensureZeroCruisingSpeedThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 0.0, "a320.jpg", 180));
    }

    @Test
    void ensureZeroSeatingCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 0));
    }

    @Test
    void ensureModelCanBeCreatedWithoutImage() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
        assertNull(model.getImagePath());

        AircraftModel model2 = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, " ", 180);
        assertEquals(" ", model2.getImagePath());
    }
}
