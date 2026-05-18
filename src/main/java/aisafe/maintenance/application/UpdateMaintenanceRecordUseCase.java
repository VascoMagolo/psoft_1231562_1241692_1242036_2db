package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.maintenance.application.dtos.UpdateMaintenanceRecordsRequest;
import aisafe.maintenance.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates an existing maintenance record with respective status and notes for it
 */
@UseCase
@Transactional
public class UpdateMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository recordRepository;
    private final MaintenancePartRepository partRepository;
    private final MaintenanceTemplateRepository templateRepository;

    public UpdateMaintenanceRecordUseCase(MaintenanceRecordRepository recordRepository, MaintenancePartRepository partRepository, MaintenanceTemplateRepository templateRepository) {
        this.recordRepository = recordRepository;
        this.partRepository = partRepository;
        this.templateRepository = templateRepository;
    }

    public MaintenanceRecord execute(UpdateMaintenanceRecordsRequest request) {
        MaintenancePart part = partRepository.findByPartNumber(request.part())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part '" + request.part() + "' not found."));
        MaintenanceTemplate template = templateRepository.findByName(request.template())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template '" + request.template() + "' not found."));
        if (request.status() == null) {
            throw new MaintenanceInvalidFieldException("Status cannot be empty.");
        }
        if (request.notes() == null || request.notes().trim().isEmpty()) {
            throw new MaintenanceInvalidFieldException("Notes cannot be empty.");
        }
        MaintenanceRecord record = recordRepository.findByStartDateAndPartAndTemplate(request.startDate(),part,template)
                .orElseThrow(() -> new MaintenanceRecordNotFoundException("Maintenance record with the specified start date, part, and template not found."));
        record.setStatus(request.status());
        record.setNotes(request.notes());
        return recordRepository.save(record);
    }

}
