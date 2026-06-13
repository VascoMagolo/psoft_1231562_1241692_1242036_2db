package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftModel;

public class AircraftModelMapper {

    public static AircraftModel toDomain(AircraftModelJpaEntity entity) {
        if (entity == null) return null;

        return new AircraftModel(
                entity.getModelName(),
                entity.getManufacturer(),
                entity.getFuelCapacity(),
                entity.getMaxRange(),
                entity.getCruisingSpeed(),
                entity.getImagePath(),
                entity.getMaximumSeatingCapacity()
        );
    }

    public static AircraftModelJpaEntity toJpa(AircraftModel domain) {
        if (domain == null) return null;

        AircraftModelJpaEntity entity = new AircraftModelJpaEntity();

        entity.setModelName(domain.getModelName());
        entity.setManufacturer(domain.getManufacturer());
        entity.setFuelCapacity(domain.getFuelCapacity());
        entity.setMaxRange(domain.getMaxRange());
        entity.setCruisingSpeed(domain.getCruisingSpeed());
        entity.setMaximumSeatingCapacity(domain.getMaximumSeatingCapacity());
        entity.setImagePath(domain.getImagePath());

        return entity;
    }
}