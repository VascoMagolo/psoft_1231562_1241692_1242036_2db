package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.AircraftStatus;
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
    private AircraftStatus status;
    private Integer seatCapacity;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private AircraftModel model;
    private List<String> features;
}
