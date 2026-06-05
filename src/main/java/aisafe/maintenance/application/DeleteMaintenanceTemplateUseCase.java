package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateNotFoundException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class DeleteMaintenanceTemplateUseCase {
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;

    public DeleteMaintenanceTemplateUseCase(MaintenanceTemplateRepository maintenanceTemplateRepository) {
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
    }

    public void execute(Long id) {
        MaintenanceTemplate template = maintenanceTemplateRepository.findById(id)
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Maintenance template not found with id: " + id));
        maintenanceTemplateRepository.delete(template);
    }
}
