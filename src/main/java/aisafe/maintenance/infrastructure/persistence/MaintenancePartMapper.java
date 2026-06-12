package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.MaintenancePart;

public class MaintenancePartMapper {
    public static MaintenancePart toDomain(MaintenancePartJpaEntity entity) {
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
