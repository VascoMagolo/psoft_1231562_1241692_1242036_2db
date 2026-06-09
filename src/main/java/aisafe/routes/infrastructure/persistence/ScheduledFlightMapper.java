package aisafe.routes.infrastructure.persistence;

import aisafe.aircrafts.infrastructure.persistence.AircraftMapper;
import aisafe.aircrafts.infrastructure.persistence.AircraftModelMapper;
import aisafe.routes.domain.ScheduledFlight;

public class ScheduledFlightMapper {

    public static ScheduledFlight toDomain(ScheduledFlightJpaEntity entity) {
        if (entity == null) return null;
        
        ScheduledFlight flight = new ScheduledFlight(
            entity.getDepartureDateTime(),
            entity.getArrivalDateTime(),
            entity.getStatus(),
            RouteMapper.toDomain(entity.getRoute()),
            AircraftMapper.toDomain(entity.getAircraft(), AircraftModelMapper.toDomain(entity.getAircraft().getModel()))
        );
        flight.setId(entity.getId());
        return flight;
    }

    public static ScheduledFlightJpaEntity toJpa(ScheduledFlight domain) {
        if (domain == null) return null;
        
        ScheduledFlightJpaEntity entity = new ScheduledFlightJpaEntity(
            domain.getDepartureDateTime(),
            domain.getArrivalDateTime(),
            domain.getStatus(),
            RouteMapper.toJpa(domain.getRoute()),
            AircraftMapper.toJpa(domain.getAircraft(), AircraftModelMapper.toJpa(domain.getAircraft().getModel()))
        );
        entity.setId(domain.getId());
        return entity;
    }
}
