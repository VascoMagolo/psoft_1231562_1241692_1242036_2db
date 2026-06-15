package aisafe.aircrafts.domain;

/**
 * Represents an aircraft model template. (Pure Domain Model)
 * It stores the manufacturer data and capacity limits used when registering individual aircraft.
 */
public class AircraftModel {

    private final String modelName;
    private final Manufacturer manufacturer;
    private Double fuelCapacity;
    private Double maxRange;
    private Double cruisingSpeed;
    private Integer maximumSeatingCapacity;
    private String imagePath;

    public AircraftModel(String modelName, Manufacturer manufacturer, Double fuelCapacity, Double maxRange, Double cruisingSpeed, String imagePath, Integer maximumSeatingCapacity) {
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
        this.imagePath = imagePath;
        this.maximumSeatingCapacity = maximumSeatingCapacity;
    }

    public String getModelName() { return modelName; }
    public Manufacturer getManufacturer() { return manufacturer; }
    public Double getFuelCapacity() { return fuelCapacity; }
    public Double getMaxRange() { return maxRange; }
    public Double getCruisingSpeed() { return cruisingSpeed; }
    public Integer getMaximumSeatingCapacity() { return maximumSeatingCapacity; }
    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setFuelCapacity(Double fuelCapacity) {
        if (fuelCapacity == null) throw new AircraftInvalidFieldException("fuelCapacity must not be null");
        if (fuelCapacity <= 0) throw new AircraftInvalidFieldException("fuelCapacity must be greater than zero");
        this.fuelCapacity = fuelCapacity;
    }

    public void setMaxRange(Double maxRange) {
        if (maxRange == null) throw new AircraftInvalidFieldException("maxRange must not be null");
        if (maxRange <= 0) throw new AircraftInvalidFieldException("maxRange must be greater than zero");
        this.maxRange = maxRange;
    }

    public void setCruisingSpeed(Double cruisingSpeed) {
        if (cruisingSpeed == null) throw new AircraftInvalidFieldException("cruisingSpeed must not be null");
        if (cruisingSpeed <= 0) throw new AircraftInvalidFieldException("cruisingSpeed must be greater than zero");
        this.cruisingSpeed = cruisingSpeed;
    }

    public void setMaximumSeatingCapacity(Integer maximumSeatingCapacity) {
        if (maximumSeatingCapacity == null) throw new AircraftInvalidFieldException("maximumSeatingCapacity must not be null");
        if (maximumSeatingCapacity <= 0) throw new AircraftInvalidFieldException("maximumSeatingCapacity must be greater than zero");
        this.maximumSeatingCapacity = maximumSeatingCapacity;
    }
}