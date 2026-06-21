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

    @Test
    void ensureNullModelNameThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel(null, Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, 180));
    }

    @Test
    void ensureNullFuelCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, null, 6150.0, 833.0, null, 180));
    }

    @Test
    void ensureNullMaxRangeThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, null, 833.0, null, 180));
    }

    @Test
    void ensureNullCruisingSpeedThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, null, null, 180));
    }

    @Test
    void ensureNullMaximumSeatingCapacityThrowsException() {
        assertThrows(AircraftInvalidFieldException.class, () ->
                new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, null, null));
    }

    @Test
    void ensureUpdateDetailsUpdatesFields() {
        AircraftModel model = validModel();
        byte[] bytes = new byte[]{1, 2, 3};
        AircraftModelImage newImage = new AircraftModelImage(bytes, "image/png");

        model.updateDetails(30000.0, 7000.0, 900.0, 200, newImage);

        assertEquals(30000.0, model.getFuelCapacity());
        assertEquals(7000.0, model.getMaxRange());
        assertEquals(900.0, model.getCruisingSpeed());
        assertEquals(200, model.getMaximumSeatingCapacity());
        assertEquals(newImage, model.getImage());
    }

    @Test
    void ensureUpdateDetailsKeepsFieldsWhenNull() {
        AircraftModel model = validModel();

        model.updateDetails(null, null, null, null, null);

        assertEquals(26730.0, model.getFuelCapacity());
        assertEquals(6150.0, model.getMaxRange());
        assertEquals(833.0, model.getCruisingSpeed());
        assertEquals(180, model.getMaximumSeatingCapacity());
        assertNull(model.getImage());
    }

    @Test
    void ensureUpdateDetailsThrowsOnInvalidValues() {
        AircraftModel model = validModel();

        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(0.0, null, null, null, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnZeroMaxRange() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, 0.0, null, null, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnNegativeMaxRange() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, -100.0, null, null, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnZeroCruisingSpeed() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, null, 0.0, null, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnNegativeCruisingSpeed() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, null, -50.0, null, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnZeroSeatingCapacity() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, null, null, 0, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnNegativeSeatingCapacity() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(null, null, null, -10, null));
    }

    @Test
    void ensureUpdateDetailsThrowsOnNegativeFuelCapacity() {
        AircraftModel model = validModel();
        assertThrows(AircraftInvalidFieldException.class, () ->
                model.updateDetails(-500.0, null, null, null, null));
    }
}
