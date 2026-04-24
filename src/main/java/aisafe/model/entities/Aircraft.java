package aisafe.model.entities;

import aisafe.model.enums.AircraftStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Column(updatable = false)
    private LocalDate manufacturingDate;
    @Enumerated(EnumType.STRING)
    private AircraftStatus status;
    private Integer seatCapacity;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private AircraftModel model;
    @ElementCollection
    private List<String> features;
}
