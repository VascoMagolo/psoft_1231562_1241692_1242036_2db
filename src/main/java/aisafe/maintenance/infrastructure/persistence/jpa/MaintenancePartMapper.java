package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenancePart;

public class MaintenancePartMapper {
    private MaintenancePartMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MaintenancePart toDomain(MaintenancePartJpaEntity entity) {
        if (entity == null) return null;
        return new MaintenancePart(
                entity.getPartNumber(), entity.getName(), entity.getDescription(),
                entity.getStockQuantity(), entity.getMinimumThreshold(), entity.getComponent());
    }

    public static MaintenancePartJpaEntity toJpa(MaintenancePart part) {
        return new MaintenancePartJpaEntity(
                part.getPartNumber(), part.getName(), part.getDescription(),
                part.getStockQuantity(), part.getMinimumThreshold(), part.getComponent());
    }
}
