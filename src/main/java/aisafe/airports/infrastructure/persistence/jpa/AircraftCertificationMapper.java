package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.IataCode;

public class AircraftCertificationMapper {

    private AircraftCertificationMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static AircraftCertification toDomain(AircraftCertificationJpaEntity entity) {
        if (entity == null) return null;
        IataCode airportCode = new IataCode(entity.getAirport().getIataCode().getCode());
        return new AircraftCertification(airportCode, new ModelName(entity.getAircraftModelName()));
    }
}
