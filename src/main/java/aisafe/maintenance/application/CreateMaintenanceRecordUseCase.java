package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.*;

/**
 * Use case for creating a new maintenance record in the system.
 */
@UseCase
public class CreateMaintenanceRecordUseCase {
    private final MaintenanceRecordRepository recordRepository;
    private final MaintenancePartRepository partRepository;
    private final MaintenanceTemplateRepository templateRepository;
    private final AircraftRepository aircraftRepository;

    public CreateMaintenanceRecordUseCase(MaintenanceRecordRepository recordRepository, MaintenancePartRepository partRepository, MaintenanceTemplateRepository templateRepository, AircraftRepository aircraftRepository) {
        this.recordRepository = recordRepository;
        this.partRepository = partRepository;
        this.templateRepository = templateRepository;
        this.aircraftRepository = aircraftRepository;
    }

    /**
     * Creates a new maintenance record based on the provided request data and saves it to the repository.
     * @param request the request containing the details of the maintenance record to be created
     * @return a response containing information of the created maintenance record
     */
    public MaintenanceRecordResponse execute(CreateMaintenanceRecordRequest request) {
        MaintenancePart part = partRepository.findByPartNumber(request.part())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part '" + request.part() + "' not found."));
        MaintenanceTemplate template = templateRepository.findByName(request.template())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template '" + request.template() + "' not found."));
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(request.registrationNumber()))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number '" + request.registrationNumber() + "' not found."));

        MaintenanceRecord record = new MaintenanceRecord(
                request.description(), request.startDate(), request.expectedDuration(),
                part, request.notes(), template, request.status(), aircraft.getRegistrationNumber().getNumber()
        );

        if (recordRepository.existsByStartDateAndPartAndTemplate(record.getStartDate(), record.getPart(), record.getTemplate())) {
            throw new MaintenanceRecordAlreadyExistsException("Record with the same start date, part, and template already exists.");
        }

        recordRepository.save(record);

        return new MaintenanceRecordResponse(
                record.getId(), record.getDescription(), record.getStartDate(),
                record.getExpectedDuration(), record.getNotes(),
                record.getPart().getPartNumber(), record.getTemplate().getName(),
                record.getStatus().name(), request.registrationNumber(),
                record.getVersion()
        );
    }
}