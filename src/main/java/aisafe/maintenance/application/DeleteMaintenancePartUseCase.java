package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.ResourceInUseException;

@UseCase
public class DeleteMaintenancePartUseCase {
    private final MaintenancePartRepository maintenancePartRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public DeleteMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository,
                                        MaintenanceRecordRepository maintenanceRecordRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    public void execute(Long id) {
        MaintenancePart part = maintenancePartRepository.findById(id)
                .orElseThrow(() -> new MaintenancePartNotFoundException("Maintenance part not found with id: " + id));
        if (maintenanceRecordRepository.existsByPart(part)) {
            throw new ResourceInUseException("Maintenance part is referenced by existing maintenance records and cannot be deleted.");
        }
        maintenancePartRepository.delete(part);
    }
}
