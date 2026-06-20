package aisafe.flights.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.airports.domain.IataCode;
import aisafe.flights.domain.ScheduledFlight;

public class ScheduledFlightMapper {

    public static ScheduledFlight toDomain(ScheduledFlightJpaEntity entity) {
        if (entity == null) return null;

        return new ScheduledFlight(
            entity.getId(),
            entity.getDepartureDateTime(),
            entity.getArrivalDateTime(),
            entity.getStatus(),
            new IataCode(entity.getRoute().getOriginCode().getCode()),
            new IataCode(entity.getRoute().getDestinationCode().getCode()),
            new RegistrationNumber(entity.getAircraft().getRegistrationNumber().getNumber())
        );
    }
}
