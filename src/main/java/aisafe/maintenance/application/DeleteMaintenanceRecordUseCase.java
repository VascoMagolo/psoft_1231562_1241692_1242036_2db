package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordNotFoundException;
import aisafe.maintenance.domain.MaintenanceRecordRepository;

@UseCase
public class DeleteMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public DeleteMaintenanceRecordUseCase(MaintenanceRecordRepository maintenanceRecordRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    public void execute(Long id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException("Maintenance record not found with id: " + id));
        maintenanceRecordRepository.delete(record);
    }
}
