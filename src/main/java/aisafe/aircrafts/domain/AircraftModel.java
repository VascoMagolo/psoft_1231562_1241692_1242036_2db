package aisafe.aircrafts.domain;

public class AircraftModel {

    private final String modelName;
    private final Manufacturer manufacturer;
    private Double fuelCapacity;
    private Double maxRange;
    private Double cruisingSpeed;
    private Integer maximumSeatingCapacity;
    private AircraftModelImage image;

    public AircraftModel(String modelName, Manufacturer manufacturer, Double fuelCapacity, Double maxRange, Double cruisingSpeed, AircraftModelImage image, Integer maximumSeatingCapacity) {
        if (modelName == null || modelName.isBlank()) throw new AircraftInvalidFieldException("modelName must not be blank");
        if (manufacturer == null) throw new AircraftInvalidFieldException("manufacturer must not be null");
        if (fuelCapacity == null) throw new AircraftInvalidFieldException("fuelCapacity must not be null");
        if (maxRange == null) throw new AircraftInvalidFieldException("maxRange must not be null");
        if (cruisingSpeed == null) throw new AircraftInvalidFieldException("cruisingSpeed must not be null");
        if (maximumSeatingCapacity == null) throw new AircraftInvalidFieldException("maximumSeatingCapacity must not be null");

        if (maxRange <= 0) throw new AircraftInvalidFieldException("maxRange must be greater than zero");
        if (fuelCapacity <= 0) throw new AircraftInvalidFieldException("fuelCapacity must be greater than zero");
        if (cruisingSpeed <= 0) throw new AircraftInvalidFieldException("cruisingSpeed must be greater than zero");
        if (maximumSeatingCapacity <= 0) throw new AircraftInvalidFieldException("maximumSeatingCapacity must be greater than zero");

        this.modelName = modelName;
        this.manufacturer = manufacturer;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
        this.cruisingSpeed = cruisingSpeed;
        this.image = image;
        this.maximumSeatingCapacity = maximumSeatingCapacity;
    }

    public String getModelName() { return modelName; }
    public Manufacturer getManufacturer() { return manufacturer; }
    public Double getFuelCapacity() { return fuelCapacity; }
    public Double getMaxRange() { return maxRange; }
    public Double getCruisingSpeed() { return cruisingSpeed; }
    public Integer getMaximumSeatingCapacity() { return maximumSeatingCapacity; }
    public AircraftModelImage getImage() { return image; }

    public void updateDetails(Double fuelCapacity, Double maxRange, Double cruisingSpeed, Integer maximumSeatingCapacity, AircraftModelImage image) {
        if (fuelCapacity != null) this.fuelCapacity = fuelCapacity;
        if (maxRange != null) this.maxRange = maxRange;
        if (cruisingSpeed != null) this.cruisingSpeed = cruisingSpeed;
        if (maximumSeatingCapacity != null) this.maximumSeatingCapacity = maximumSeatingCapacity;
        if (image != null) this.image = image;

        validateFields(this.fuelCapacity, this.maxRange, this.cruisingSpeed, this.maximumSeatingCapacity);
    }

    private void validateFields(Double fuelCapacity, Double maxRange, Double cruisingSpeed, Integer maximumSeatingCapacity) {
        if (fuelCapacity == null || fuelCapacity <= 0) throw new AircraftInvalidFieldException("fuelCapacity must be greater than zero");
        if (maxRange == null || maxRange <= 0) throw new AircraftInvalidFieldException("maxRange must be greater than zero");
        if (cruisingSpeed == null || cruisingSpeed <= 0) throw new AircraftInvalidFieldException("cruisingSpeed must be greater than zero");
        if (maximumSeatingCapacity == null || maximumSeatingCapacity <= 0) throw new AircraftInvalidFieldException("maximumSeatingCapacity must be greater than zero");
    }
}
