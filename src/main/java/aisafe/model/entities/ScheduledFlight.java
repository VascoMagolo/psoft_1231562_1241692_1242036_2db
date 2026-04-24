package aisafe.model.entities;

import aisafe.model.enums.FlightStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
/**
 * Domain model for a scheduled flight.
 * <p>
 * The scheduled departure time is represented as an {@link OffsetDateTime} to
 * make the model explicitly offset-aware rather than a generic local date-time.
 */
@Entity
public class ScheduledFlight {
    /**
     * Scheduled departure timestamp, including the UTC offset.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private OffsetDateTime  departureDateTime;
    @Enumerated(EnumType.STRING)
    private FlightStatus status;

}
