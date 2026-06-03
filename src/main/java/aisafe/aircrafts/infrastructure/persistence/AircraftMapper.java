package aisafe.aircrafts.infrastructure.persistence;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;

public class AircraftMapper {

    public static Aircraft toDomain(AircraftJpaEntity entity, AircraftModel pureModel) {
        if (entity == null) return null;

        return new Aircraft(
                AircraftStatus.valueOf(entity.getStatus()),
                entity.getManufacturingDate(),
                pureModel,
                new RegistrationNumber(entity.getRegistrationNumber()),
                entity.getSeatCapacity(),
                entity.getFeatures()
        );
    }

    public static AircraftJpaEntity toJpa(Aircraft domain, AircraftModelJpaEntity jpaModel) {
        if (domain == null) return null;

        AircraftJpaEntity entity = new AircraftJpaEntity();
        entity.setRegistrationNumber(domain.getRegistrationNumber().getNumber());
        entity.setManufacturingDate(domain.getManufacturingDate());
        entity.setStatus(domain.getStatus().name());
        entity.setSeatCapacity(domain.getSeatCapacity());
        entity.setModel(jpaModel);
        entity.setFeatures(domain.getFeatures());

        return entity;
    }
}