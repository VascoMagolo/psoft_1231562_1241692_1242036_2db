package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordNotFoundException;
import aisafe.maintenance.domain.MaintenanceRecordRepository;

import java.util.UUID;

@UseCase
public class DeleteMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public DeleteMaintenanceRecordUseCase(MaintenanceRecordRepository maintenanceRecordRepository) {
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    public void execute(UUID recordId) {
        MaintenanceRecord record = maintenanceRecordRepository.findByRecordId(recordId)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException("Maintenance record not found with id: " + recordId));
        maintenanceRecordRepository.delete(record);
    }
}
