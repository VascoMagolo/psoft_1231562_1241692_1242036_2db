package aisafe.airports.domain;

import aisafe.aircrafts.domain.AircraftModel;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"airport_id", "aircraft_model_id"}))
public class AirplaneCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_model_id", nullable = false)
    private AircraftModel aircraftModel;

    protected AirplaneCertification() {}

    public AirplaneCertification(Airport airport, AircraftModel aircraftModel) {
        this.airport = airport;
        this.aircraftModel = aircraftModel;
    }
}
