package aisafe.aircrafts.domain;

import org.springframework.util.Assert;

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
        Assert.hasText(modelName, "modelName must not be blank");
        Assert.notNull(manufacturer, "manufacturer must not be null");
        Assert.notNull(fuelCapacity, "fuelCapacity must not be null");
        Assert.notNull(maxRange, "maxRange must not be null");
        Assert.notNull(cruisingSpeed, "cruisingSpeed must not be null");
        Assert.notNull(maximumSeatingCapacity, "maximumSeatingCapacity must not be null");
        Assert.hasText(imagePath, "imagePath must not be blank");

        Assert.isTrue(maxRange > 0, "maxRange must be greater than zero");
        Assert.isTrue(fuelCapacity > 0, "fuelCapacity must be greater than zero");
        Assert.isTrue(cruisingSpeed > 0, "cruisingSpeed must be greater than zero");
        Assert.isTrue(maximumSeatingCapacity > 0, "maximumSeatingCapacity must be greater than zero");

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
        Assert.hasText(imagePath, "imagePath must not be blank");
        this.imagePath = imagePath;
    }

    public void setFuelCapacity(Double fuelCapacity) {
        Assert.notNull(fuelCapacity, "fuelCapacity must not be null");
        Assert.isTrue(fuelCapacity > 0, "fuelCapacity must be greater than zero");
        this.fuelCapacity = fuelCapacity;
    }

    public void setMaxRange(Double maxRange) {
        Assert.notNull(maxRange, "maxRange must not be null");
        Assert.isTrue(maxRange > 0, "maxRange must be greater than zero");
        this.maxRange = maxRange;
    }

    public void setCruisingSpeed(Double cruisingSpeed) {
        Assert.notNull(cruisingSpeed, "cruisingSpeed must not be null");
        Assert.isTrue(cruisingSpeed > 0, "cruisingSpeed must be greater than zero");
        this.cruisingSpeed = cruisingSpeed;
    }

    public void setMaximumSeatingCapacity(Integer maximumSeatingCapacity) {
        Assert.notNull(maximumSeatingCapacity, "maximumSeatingCapacity must not be null");
        Assert.isTrue(maximumSeatingCapacity > 0, "maximumSeatingCapacity must be greater than zero");
        this.maximumSeatingCapacity = maximumSeatingCapacity;
    }
}