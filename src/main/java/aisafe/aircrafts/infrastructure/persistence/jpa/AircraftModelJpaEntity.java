package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.Manufacturer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aircraft_models")
@Getter
@Setter
public class AircraftModelJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String modelName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private Double fuelCapacity;

    @Column(nullable = false)
    private Double maxRange;

    @Column(nullable = false)
    private Double cruisingSpeed;

    @Column(nullable = false)
    private Integer maximumSeatingCapacity;

    private String imagePath;

    public AircraftModelJpaEntity() {}
}