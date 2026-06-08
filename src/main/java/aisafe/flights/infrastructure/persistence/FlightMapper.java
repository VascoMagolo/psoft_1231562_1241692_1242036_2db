package aisafe.flights.infrastructure.persistence;

import aisafe.flights.domain.Flight;

public class FlightMapper {

    public static Flight toDomain(FlightJpaEntity entity) {
        Flight flight = new Flight(
                entity.getAircraftRegistrationNumber(),
                entity.getRouteId(),
                entity.getDepartureDateTime(),
                entity.getArrivalDateTime()
        );
        flight.setId(entity.getId());
        return flight;
    }

    public static FlightJpaEntity toJpa(Flight flight) {
        FlightJpaEntity entity = new FlightJpaEntity(
                flight.getAircraftRegistrationNumber(),
                flight.getRouteId(),
                flight.getDepartureDateTime(),
                flight.getArrivalDateTime()
        );
        entity.setId(flight.getId());
        return entity;
    }
}
