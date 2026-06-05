package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class DeleteMaintenancePartUseCase {
    private final MaintenancePartRepository maintenancePartRepository;

    public DeleteMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
    }

    public void execute(Long id) {
        MaintenancePart part = maintenancePartRepository.findById(id)
                .orElseThrow(() -> new MaintenancePartNotFoundException("Maintenance part not found with id: " + id));
        maintenancePartRepository.delete(part);
    }
}
