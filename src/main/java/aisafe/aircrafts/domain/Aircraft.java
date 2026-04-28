package aisafe.aircrafts.domain;

import aisafe.model.enums.AircraftStatus;
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
    private String registrationNumber;

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
    private List<String> features;
}
