package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenanceTemplateResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceTemplateRequest;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateNotFoundException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.shared.application.UseCase;

@UseCase
public class UpdateMaintenanceTemplateUseCase {

    private final MaintenanceTemplateRepository maintenanceTemplateRepository;

    public UpdateMaintenanceTemplateUseCase(MaintenanceTemplateRepository maintenanceTemplateRepository) {
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
    }

    public MaintenanceTemplateResponse execute(String templateName, UpdateMaintenanceTemplateRequest request) {
        MaintenanceTemplate template = maintenanceTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template not found: " + templateName));

        template.updateDetails(request.checklist(), request.intervalFlightHours(), request.intervalDays());

        maintenanceTemplateRepository.save(template);

        return new MaintenanceTemplateResponse(
                template.getName(),
                template.getTemplateType().name()
        );
    }
}
