package aisafe.aircrafts.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    private AircraftModel validModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
    }

    @Test
    void ensureValidModelIsCreated() {
        AircraftModel model = validModel();
        assertEquals("A320", model.getModelName());
        assertEquals(Manufacturer.AIRBUS, model.getManufacturer());
        assertEquals(180, model.getMaximumSeatingCapacity());
    }

    @Test
    void ensureBlankModelNameThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("  ", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180));
    }

    @Test
    void ensureNullManufacturerThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", null, 26730.0, 6150.0, 833.0, null, 180));
    }

    @Test
    void ensureZeroMaxRangeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 0.0, 833.0, null, 180));
    }

    @Test
    void ensureNegativeMaxRangeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, -100.0, 833.0, null, 180));
    }

    @Test
    void ensureZeroFuelCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 0.0, 6150.0, 833.0, null, 180));
    }

    @Test
    void ensureZeroCruisingSpeedThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 0.0, null, 180));
    }

    @Test
    void ensureZeroSeatingCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 0));
    }

    @Test
    void ensureModelCanBeCreatedWithoutImage() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180);
        assertNull(model.getImage());
    }

    @Test
    void ensureModelCanBeCreatedWithImage() {
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage image = new AircraftModelImage(bytes, "image/jpeg");
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, image, 180);
        assertEquals(image, model.getImage());
        assertEquals("image/jpeg", model.getImage().getContentType());
    }
}
