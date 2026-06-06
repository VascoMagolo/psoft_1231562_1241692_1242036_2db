package aisafe.routes.domain;

import java.time.OffsetDateTime;

/**
 * Domain model for a scheduled flight.
 * <p>
 * The scheduled departure time is represented as an {@link OffsetDateTime} to
 * make the model explicitly offset-aware rather than a generic local date-time.
 */
public class ScheduledFlight {
    /**
     * Scheduled departure timestamp, including the UTC offset.
     */
    private Long id;
    private OffsetDateTime departureDateTime;
    private FlightStatus status;
}
