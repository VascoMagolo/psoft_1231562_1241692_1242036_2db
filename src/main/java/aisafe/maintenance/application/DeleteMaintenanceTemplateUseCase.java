package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateNotFoundException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import aisafe.shared.domain.ResourceInUseException;

@UseCase
public class DeleteMaintenanceTemplateUseCase {
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public DeleteMaintenanceTemplateUseCase(MaintenanceTemplateRepository maintenanceTemplateRepository,
                                            MaintenanceRecordRepository maintenanceRecordRepository) {
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    public void execute(Long id) {
        MaintenanceTemplate template = maintenanceTemplateRepository.findById(id)
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Maintenance template not found with id: " + id));
        if (maintenanceRecordRepository.existsByTemplate(template)) {
            throw new ResourceInUseException("Maintenance template is referenced by existing maintenance records and cannot be deleted.");
        }
        maintenanceTemplateRepository.delete(template);
    }
}
