package aisafe.aircrafts.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents an individual aircraft tracked by the system, including its registration number, model, status, seat capacity, manufacturing date, and configured features.
 */
@Getter
@Entity
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Version
    private Long version;
    @Embedded
    private RegistrationNumber registrationNumber;

    @Column(updatable = false)
    private LocalDate manufacturingDate;
    @Enumerated(EnumType.STRING)
    @Setter
    private AircraftStatus status;
    private Integer seatCapacity;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private AircraftModel model;
    @ElementCollection
    @CollectionTable(name = "aircraft_features", joinColumns = @JoinColumn(name = "aircraft_id"))
    @Column(name = "feature")
    private List<String> features;

    public Aircraft() {}

    public Aircraft(AircraftStatus status, LocalDate manufacturingDate, AircraftModel model, RegistrationNumber registrationNumber, Integer seatCapacity, List<String> features) {
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
        this.features = features;
    }
}
