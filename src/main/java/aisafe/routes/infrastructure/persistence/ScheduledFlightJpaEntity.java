package aisafe.routes.infrastructure.persistence;

import aisafe.routes.domain.FlightStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "scheduled_flight")
public class ScheduledFlightJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime departureDateTime;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    protected ScheduledFlightJpaEntity() {}
}
