package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.MaintenanceRecord;

public class MaintenanceRecordMapper {
    public static MaintenanceRecord toDomain(MaintenanceRecordJpaEntity entity) {
        MaintenanceRecord record = new MaintenanceRecord(
                entity.getDescription(), entity.getStartDate(), entity.getExpectedDuration(),
                MaintenancePartMapper.toDomain(entity.getPart()), entity.getNotes(),
                MaintenanceTemplateMapper.toDomain(entity.getTemplate()),
                entity.getStatus(), entity.getAircraftRegistration());
        record.setId(entity.getId());
        record.setVersion(entity.getVersion());
        return record;
    }
}
