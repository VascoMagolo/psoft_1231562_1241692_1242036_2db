package aisafe.aircrafts.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "aircrafts")
public class AircraftJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer seatCapacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private AircraftModelJpaEntity model;

    @ElementCollection
    @CollectionTable(name = "aircraft_features", joinColumns = @JoinColumn(name = "aircraft_id"))
    @Column(name = "feature")
    private List<String> features;

    public AircraftJpaEntity() {}
}