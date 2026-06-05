package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.MaintenanceTemplate;

public class MaintenanceTemplateMapper {
    public static MaintenanceTemplate toDomain(MaintenanceTemplateJpaEntity entity) {
        MaintenanceTemplate template = new MaintenanceTemplate(
                entity.getName(), entity.getTemplateType(), entity.getApplicableModelNames(),
                entity.getChecklist(), entity.getIntervalFlightHours(), entity.getIntervalDays());
        template.setId(entity.getId());
        return template;
    }

    public static MaintenanceTemplateJpaEntity toJpa(MaintenanceTemplate template) {
        return new MaintenanceTemplateJpaEntity(
                template.getName(), template.getTemplateType(), template.getApplicableModelNames(),
                template.getChecklist(), template.getIntervalFlightHours(), template.getIntervalDays());
    }
}
