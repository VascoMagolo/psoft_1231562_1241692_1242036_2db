package aisafe.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
