package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenanceRecordsRequest;
import aisafe.maintenance.domain.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates an existing maintenance record with respective status and notes for it
 */
@UseCase
@Transactional
public class UpdateMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository recordRepository;

    public UpdateMaintenanceRecordUseCase(MaintenanceRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    /**
     * Updates the status and notes of an existing maintenance record based on the provided request data and saves it to the repository.
     * @param id the ID of the maintenance record to be updated
     * @param request the request containing the new status and notes for the maintenance record
     * @param clientVersion the version of the maintenance record that the client has, used for optimistic locking
     * @return a response containing information of the updated maintenance record
     */
    public MaintenanceRecordResponse execute(Long id, UpdateMaintenanceRecordsRequest request, Long clientVersion) {
        if (request.status() == null) {
            throw new MaintenanceInvalidFieldException("Status cannot be empty.");
        }

        MaintenanceRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException("Maintenance record with ID " + id + " not found."));

        if (!record.getVersion().equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(MaintenanceRecord.class, record.getId());
        }

        record.setStatus(request.status());
        if (request.notes() != null && !request.notes().trim().isEmpty()) {
            record.setNotes(request.notes());
        }

        MaintenanceRecord updatedRecord = recordRepository.save(record);

        return new MaintenanceRecordResponse(
                updatedRecord.getId(), updatedRecord.getDescription(), updatedRecord.getStartDate(),
                updatedRecord.getExpectedDuration(), updatedRecord.getNotes(),
                updatedRecord.getPart().getPartNumber(), updatedRecord.getTemplate().getName(),
                updatedRecord.getStatus().name(), updatedRecord.getAircraft().getRegistrationNumber().getNumber(),
                updatedRecord.getVersion()
        );
    }
}