package aisafe.flights.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import java.time.OffsetDateTime;
import java.time.Duration;

public class ScheduledFlight {
    private Long id;
    private OffsetDateTime departureDateTime;
    private OffsetDateTime arrivalDateTime;
    private FlightStatus status;
    private IataCode originCode;
    private IataCode destinationCode;
    private RegistrationNumber aircraftRegistrationNumber;

    public ScheduledFlight(OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime,
                           FlightStatus status, IataCode originCode, IataCode destinationCode,
                           RegistrationNumber aircraftRegistrationNumber) {
        if (departureDateTime == null) {
            throw new InvalidFlightScheduleException("Departure date/time must not be null.");
        }
        if (arrivalDateTime == null) {
            throw new InvalidFlightScheduleException("Arrival date/time must not be null.");
        }
        if (status == null) {
            throw new InvalidFlightScheduleException("Flight status must not be null.");
        }
        if (originCode == null) {
            throw new InvalidFlightScheduleException("Origin IATA code must not be null.");
        }
        if (destinationCode == null) {
            throw new InvalidFlightScheduleException("Destination IATA code must not be null.");
        }
        if (aircraftRegistrationNumber == null) {
            throw new InvalidFlightScheduleException("Aircraft registration number must not be null.");
        }
        if (!departureDateTime.isBefore(arrivalDateTime)) {
            throw new InvalidFlightScheduleException("Departure must be before arrival.");
        }
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.status = status;
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.aircraftRegistrationNumber = aircraftRegistrationNumber;
    }

    public ScheduledFlight(Long id, OffsetDateTime departureDateTime, OffsetDateTime arrivalDateTime,
                           FlightStatus status, IataCode originCode, IataCode destinationCode,
                           RegistrationNumber aircraftRegistrationNumber) {
        this(departureDateTime, arrivalDateTime, status, originCode, destinationCode, aircraftRegistrationNumber);
        this.id = id;
    }

    public Long getId() { return id; }

    public OffsetDateTime getDepartureDateTime() { return departureDateTime; }
    public OffsetDateTime getArrivalDateTime() { return arrivalDateTime; }
    public FlightStatus getStatus() { return status; }
    public IataCode getOriginCode() { return originCode; }
    public IataCode getDestinationCode() { return destinationCode; }
    public RegistrationNumber getAircraftRegistrationNumber() { return aircraftRegistrationNumber; }

    public Duration getDuration() {
        if (departureDateTime == null || arrivalDateTime == null) return Duration.ZERO;
        return Duration.between(departureDateTime, arrivalDateTime);
    }
}
