package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceRecord;

public class MaintenanceRecordMapper {
    public static MaintenanceRecord toDomain(MaintenanceRecordJpaEntity entity) {
        if (entity == null) return null;
        MaintenanceRecord record = new MaintenanceRecord(
                entity.getRecordId(), entity.getDescription(), entity.getStartDate(), entity.getExpectedDuration(),
                entity.getParts().stream().map(MaintenancePartMapper::toDomain).toList(), entity.getNotes(),
                MaintenanceTemplateMapper.toDomain(entity.getTemplate()),
                entity.getStatus(), entity.getAircraftRegistration());
        record.setVersion(entity.getVersion());
        return record;
    }
}
