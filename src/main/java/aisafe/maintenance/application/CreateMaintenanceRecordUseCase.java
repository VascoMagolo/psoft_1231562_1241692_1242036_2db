package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.CreateMaintenanceRecordRequest;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public MaintenanceRecordResponse execute(CreateMaintenanceRecordRequest request) {
        List<MaintenancePart> parts = request.parts().stream()
                .map(pn -> partRepository.findByPartNumber(pn)
                        .orElseThrow(() -> new MaintenancePartNotFoundException("Part '" + pn + "' not found.")))
                .collect(Collectors.toList());
        MaintenanceTemplate template = templateRepository.findByName(request.template())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template '" + request.template() + "' not found."));
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(request.registrationNumber()))
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number '" + request.registrationNumber() + "' not found."));

        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), request.description(), request.startDate(), request.expectedDuration(),
                parts, request.notes(), template, request.status(), aircraft.getRegistrationNumber().getNumber()
        );

        if (recordRepository.existsByStartDateAndTemplate(record.getStartDate(), record.getTemplate())) {
            throw new MaintenanceRecordAlreadyExistsException("Record with the same start date and template already exists.");
        }

        recordRepository.save(record);

        return new MaintenanceRecordResponse(
                record.getRecordId(), record.getDescription(), record.getStartDate(),
                record.getExpectedDuration(), record.getNotes(),
                record.getParts().stream().map(MaintenancePart::getPartNumber).collect(Collectors.toList()),
                record.getTemplate().getName(),
                record.getStatus().name(), request.registrationNumber(),
                record.getVersion()
        );
    }
}
