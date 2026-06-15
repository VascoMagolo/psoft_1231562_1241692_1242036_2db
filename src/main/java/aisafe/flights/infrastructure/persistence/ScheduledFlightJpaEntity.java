package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.aircrafts.infrastructure.persistence.jpa.AircraftJpaEntity;
import aisafe.routes.domain.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "scheduled_flight")
@Getter
@Setter
public class ScheduledFlightJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime departureDateTime;

    @Column(nullable = false)
    private OffsetDateTime arrivalDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteJpaEntity route;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private AircraftJpaEntity aircraft;

    @Version
    private Long version;

    public ScheduledFlightJpaEntity() {}

    public ScheduledFlightJpaEntity(OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime, 
                                   FlightStatus status, RouteJpaEntity route, AircraftJpaEntity aircraft) {
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.status = status;
        this.route = route;
        this.aircraft = aircraft;
    }
}
