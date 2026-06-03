package aisafe.aircrafts.domain;

import org.springframework.util.Assert;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an individual aircraft tracked by the system.
 */
public class Aircraft {

    private final RegistrationNumber registrationNumber;
    private final LocalDate manufacturingDate;
    private AircraftStatus status;
    private final Integer seatCapacity;
    private final AircraftModel model;
    private final List<String> features;

    public Aircraft(AircraftStatus status, LocalDate manufacturingDate, AircraftModel model,
                    RegistrationNumber registrationNumber, Integer seatCapacity, List<String> features) {
        Assert.notNull(status, "Status must not be null.");
        Assert.notNull(manufacturingDate, "Manufacturing date must not be null.");
        Assert.notNull(model, "Model must not be null.");
        Assert.notNull(registrationNumber, "Registration number must not be null.");
        Assert.notNull(seatCapacity, "Seat capacity must not be null.");
        Assert.isTrue(seatCapacity > 0, "Seat capacity must be greater than zero.");

        this.status = status;
        this.manufacturingDate = manufacturingDate;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.seatCapacity = seatCapacity;
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }

    public void changeStatus(AircraftStatus newStatus) {
        Assert.notNull(newStatus, "New status must not be null.");
        this.status = newStatus;
    }

    public RegistrationNumber getRegistrationNumber() { return registrationNumber; }
    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public AircraftStatus getStatus() { return status; }
    public Integer getSeatCapacity() { return seatCapacity; }
    public AircraftModel getModel() { return model; }
    public List<String> getFeatures() { return Collections.unmodifiableList(features); }
}