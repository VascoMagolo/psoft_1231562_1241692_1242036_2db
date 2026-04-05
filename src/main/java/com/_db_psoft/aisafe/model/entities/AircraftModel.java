package com._db_psoft.aisafe.model.entities;

import jakarta.persistence.*;

@Entity
public class AircraftModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String manufacturer;
    private Double fuelCapacity;
    private Double maxRange;
    private Double cruisingSpeed;
    private String imagePath;
}
