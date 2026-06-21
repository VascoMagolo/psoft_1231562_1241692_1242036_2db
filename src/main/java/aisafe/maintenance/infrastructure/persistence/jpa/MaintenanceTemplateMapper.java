package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.maintenance.domain.MaintenanceTemplate;

import java.util.stream.Collectors;

public class MaintenanceTemplateMapper {
    private MaintenanceTemplateMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MaintenanceTemplate toDomain(MaintenanceTemplateJpaEntity entity) {
        return new MaintenanceTemplate(
                entity.getName(),
                entity.getTemplateType(),
                entity.getApplicableModelNames().stream().map(ModelName::new).collect(Collectors.toList()),
                entity.getChecklist(),
                entity.getIntervalFlightHours(),
                entity.getIntervalDays());
    }

    public static MaintenanceTemplateJpaEntity toJpa(MaintenanceTemplate template) {
        return new MaintenanceTemplateJpaEntity(
                template.getName(),
                template.getTemplateType(),
                template.getApplicableModelNames().stream().map(ModelName::getName).collect(Collectors.toList()),
                template.getChecklist(),
                template.getIntervalFlightHours(),
                template.getIntervalDays());
    }
}
