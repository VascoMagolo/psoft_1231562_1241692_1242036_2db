package aisafe.aircrafts.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
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
        this.status = status;
        this.manufacturingDate = manufacturingDate;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.seatCapacity = seatCapacity;
        this.features = features;
    }
}
