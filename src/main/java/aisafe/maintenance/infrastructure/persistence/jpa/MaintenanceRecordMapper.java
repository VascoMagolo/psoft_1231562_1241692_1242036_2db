package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.MaintenanceRecord;

public class MaintenanceRecordMapper {
    public static MaintenanceRecord toDomain(MaintenanceRecordJpaEntity entity) {
        if (entity == null) return null;
        return new MaintenanceRecord(
                entity.getRecordId(), entity.getDescription(), entity.getStartDate(), entity.getExpectedDuration(),
                entity.getParts().stream().map(MaintenancePartMapper::toDomain).toList(), entity.getNotes(),
                MaintenanceTemplateMapper.toDomain(entity.getTemplate()),
                entity.getStatus(), entity.getComponents(), new RegistrationNumber(entity.getAircraftRegistration()),
                entity.getCost());
    }
}
