package aisafe.aircrafts.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelImage;

public class AircraftModelMapper {

    public static AircraftModel toDomain(AircraftModelJpaEntity entity) {
        if (entity == null) return null;

        AircraftModelImage image = null;
        AircraftModelImageJpaEmbeddable embeddable = entity.getImage();
        if (embeddable != null && embeddable.getBytes() != null) {
            image = new AircraftModelImage(embeddable.getBytes(), embeddable.getContentType());
        }

        return new AircraftModel(
                entity.getModelName(),
                entity.getManufacturer(),
                entity.getFuelCapacity(),
                entity.getMaxRange(),
                entity.getCruisingSpeed(),
                image,
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

        AircraftModelImage domainImage = domain.getImage();
        if (domainImage != null) {
            AircraftModelImageJpaEmbeddable embeddable = new AircraftModelImageJpaEmbeddable();
            embeddable.setBytes(domainImage.getBytes());
            embeddable.setContentType(domainImage.getContentType());
            entity.setImage(embeddable);
        }

        return entity;
    }
}
