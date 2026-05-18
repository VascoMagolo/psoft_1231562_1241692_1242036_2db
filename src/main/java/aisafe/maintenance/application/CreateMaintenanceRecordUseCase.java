package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.domain.*;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a maintenance record for a specific Aircraft.
 * By aircraft registration number
 */
@UseCase
@Transactional
public class CreateMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository recordRepository;
    private final MaintenancePartRepository partRepository;
    private final MaintenanceTemplateRepository templateRepository;
    private final AircraftRepository aircraftRepository;
    public CreateMaintenanceRecordUseCase(MaintenanceRecordRepository maintenanceRecordRepository, MaintenancePartRepository partRepository, MaintenanceTemplateRepository templateRepository, AircraftRepository aircraftRepository) {
        this.recordRepository = maintenanceRecordRepository;
        this.partRepository = partRepository;
        this.templateRepository = templateRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public MaintenanceRecord execute(CreateMaintenanceRecordRequest request) {
        MaintenancePart part = partRepository.findByPartNumber(request.part())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part '" + request.part() + "' not found."));
        MaintenanceTemplate template = templateRepository.findByName(request.template())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template '" + request.template() + "' not found."));
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(request.registrationNumber()))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number '" + request.registrationNumber() + "' not found."));
        MaintenanceRecord record = new MaintenanceRecord(
            request.description(),
            request.startDate(),
            request.expectedDuration(),
            part,
            request.notes(),
            template,
            request.status(),
            aircraft
        );
        if (recordRepository.existsByStartDateAndPartAndTemplate(record.getStartDate(), record.getPart(), record.getTemplate())) {
            throw new MaintenanceRecordAlreadyExistsException("Record with the same start date, part, and template already exists.");
        }
        return recordRepository.save(record);
    }
}
