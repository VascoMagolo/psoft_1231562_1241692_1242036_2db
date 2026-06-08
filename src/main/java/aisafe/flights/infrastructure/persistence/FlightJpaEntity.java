package aisafe.flights.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "flight")
@Getter
@Setter
public class FlightJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aircraft_registration_number", nullable = false)
    private String aircraftRegistrationNumber;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(nullable = false)
    private OffsetDateTime departureDateTime;

    @Column(nullable = false)
    private OffsetDateTime arrivalDateTime;

    protected FlightJpaEntity() {}

    public FlightJpaEntity(String aircraftRegistrationNumber, Long routeId,
                           OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime) {
        this.aircraftRegistrationNumber = aircraftRegistrationNumber;
        this.routeId = routeId;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
    }
}
