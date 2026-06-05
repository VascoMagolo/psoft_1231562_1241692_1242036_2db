package aisafe.airports.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "aircraft_certification",
        uniqueConstraints = @UniqueConstraint(columnNames = {"airport_id", "aircraft_model_name"}))
@Getter
@Setter
public class AircraftCertificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "airport_id", nullable = false)
    private AirportJpaEntity airport;

    @Column(name = "aircraft_model_name", nullable = false)
    private String aircraftModelName;

    public AircraftCertificationJpaEntity() {}

    public AircraftCertificationJpaEntity(AirportJpaEntity airport, String aircraftModelName) {
        this.airport = airport;
        this.aircraftModelName = aircraftModelName;
    }
}
