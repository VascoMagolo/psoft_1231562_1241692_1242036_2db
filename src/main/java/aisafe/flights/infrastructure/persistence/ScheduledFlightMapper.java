package aisafe.flights.infrastructure.persistence;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.routes.infrastructure.persistence.jpa.RouteMapper;

public class ScheduledFlightMapper {

    public static ScheduledFlight toDomain(ScheduledFlightJpaEntity entity) {
        if (entity == null) return null;

        ScheduledFlight flight = new ScheduledFlight(
            entity.getDepartureDateTime(),
            entity.getArrivalDateTime(),
            entity.getStatus(),
            RouteMapper.toDomain(entity.getRoute()),
            new RegistrationNumber(entity.getAircraft().getRegistrationNumber().getNumber())
        );
        flight.setId(entity.getId());
        return flight;
    }
}
