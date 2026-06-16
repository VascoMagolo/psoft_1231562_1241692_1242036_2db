package aisafe.aircrafts.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an individual aircraft tracked by the system.
 */
public class Aircraft {

    private final RegistrationNumber registrationNumber;
    private LocalDate manufacturingDate;
    private AircraftStatus status;
    private Integer seatCapacity;
    private Double range;
    private AircraftModel model;
    private List<String> features;

    public Aircraft(AircraftStatus status, LocalDate manufacturingDate, AircraftModel model,
                    RegistrationNumber registrationNumber, Integer seatCapacity, Double range, List<String> features) {
        validateFields(status, manufacturingDate, model, registrationNumber, seatCapacity, range);

        this.status = status;
        this.manufacturingDate = manufacturingDate;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.seatCapacity = seatCapacity;
        this.range = range;
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }

    private void validateFields(AircraftStatus status, LocalDate manufacturingDate, AircraftModel model,
                                RegistrationNumber registrationNumber, Integer seatCapacity, Double range) {
        if (status == null) throw new AircraftInvalidFieldException("Status must not be null.");
        if (manufacturingDate == null) throw new AircraftInvalidFieldException("Manufacturing date must not be null.");
        if (model == null) throw new AircraftInvalidFieldException("Model must not be null.");
        if (registrationNumber == null) throw new AircraftInvalidFieldException("Registration number must not be null.");
        if (seatCapacity == null) throw new AircraftInvalidFieldException("Seat capacity must not be null.");
        if (seatCapacity <= 0) throw new AircraftInvalidFieldException("Seat capacity must be greater than zero.");
        if (range == null) throw new AircraftInvalidFieldException("Range must not be null.");
        if (range <= 0) throw new AircraftInvalidFieldException("Range must be greater than zero.");
        if (range > model.getMaxRange()) throw new AircraftInvalidFieldException("Aircraft range cannot exceed model's maximum range (" + model.getMaxRange() + ")");
    }

    public void updateDetails(AircraftModel model, LocalDate manufacturingDate, Integer seatCapacity, Double range, List<String> features, AircraftStatus status) {
        if (model != null) this.model = model;
        if (manufacturingDate != null) this.manufacturingDate = manufacturingDate;
        if (seatCapacity != null) this.seatCapacity = seatCapacity;
        if (range != null) this.range = range;
        if (features != null) this.features = new ArrayList<>(features);
        if (status != null) this.status = status;

        validateFields(this.status, this.manufacturingDate, this.model, this.registrationNumber, this.seatCapacity, this.range);
    }

    public void changeStatus(AircraftStatus newStatus) {
        if (newStatus == null) throw new AircraftInvalidFieldException("New status must not be null.");
        this.status = newStatus;
    }

    public RegistrationNumber getRegistrationNumber() { return registrationNumber; }
    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public AircraftStatus getStatus() { return status; }
    public Integer getSeatCapacity() { return seatCapacity; }
    public Double getRange() { return range; }
    public AircraftModel getModel() { return model; }
    public List<String> getFeatures() { return Collections.unmodifiableList(features); }

    public Double getFuelConsumptionPerDistanceUnit() {
        return model.getFuelCapacity() / this.range;
    }
}