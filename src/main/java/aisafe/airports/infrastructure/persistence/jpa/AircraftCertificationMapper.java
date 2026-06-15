package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.Airport;

public class AircraftCertificationMapper {

    public static AircraftCertification toDomain(AircraftCertificationJpaEntity entity) {
        if (entity == null) return null;
        Airport airport = AirportMapper.toDomain(entity.getAirport());
        return new AircraftCertification(airport, new ModelName(entity.getAircraftModelName()));
    }
}
