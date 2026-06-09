package aisafe.aircrafts.infrastructure.persistence;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.RegistrationNumber;

public class AircraftMapper {

    public static Aircraft toDomain(AircraftJpaEntity entity, AircraftModel pureModel) {
        if (entity == null) return null;

        Aircraft aircraft = new Aircraft(
                AircraftStatus.valueOf(entity.getStatus()),
                entity.getManufacturingDate(),
                pureModel,
                new RegistrationNumber(entity.getRegistrationNumber().getNumber()),
                entity.getSeatCapacity(),
                entity.getRange(),
                entity.getFeatures()
        );
        aircraft.setVersion(entity.getVersion());
        return aircraft;
    }

    public static AircraftJpaEntity toJpa(Aircraft domain, AircraftModelJpaEntity jpaModel) {
        if (domain == null) return null;

        AircraftJpaEntity entity = new AircraftJpaEntity();
        entity.setRegistrationNumber(new RegistrationNumberJpaEmbeddable(domain.getRegistrationNumber().getNumber()));
        entity.setManufacturingDate(domain.getManufacturingDate());
        entity.setStatus(domain.getStatus().name());
        entity.setSeatCapacity(domain.getSeatCapacity());
        entity.setRange(domain.getRange());
        entity.setModel(jpaModel);
        entity.setFeatures(domain.getFeatures());

        return entity;
    }
}