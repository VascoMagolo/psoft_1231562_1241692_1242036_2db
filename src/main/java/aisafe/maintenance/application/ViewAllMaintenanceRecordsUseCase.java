package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.ViewAllMaintenanceRecordsResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Views all maintenance records from a specific aircraft using its registration number
 */
@UseCase
@Transactional(readOnly = true)
public class ViewAllMaintenanceRecordsUseCase {
    private final MaintenanceRecordRepository repository;
    private final AircraftRepository aircraftRepository;
    public ViewAllMaintenanceRecordsUseCase(MaintenanceRecordRepository repository, AircraftRepository aircraftRepository) {
        this.repository = repository;
        this.aircraftRepository = aircraftRepository;
    }

    public List<ViewAllMaintenanceRecordsResponse> execute(RegistrationNumber registrationNumber) {
        aircraftRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number: " + registrationNumber + " not found."));

        return repository.findByAircraftRegistrationNumber(registrationNumber).stream()
                .map(this::toResponse)
                .toList();
    }

    private ViewAllMaintenanceRecordsResponse toResponse(MaintenanceRecord record) {
        return new ViewAllMaintenanceRecordsResponse(
                record.getPart().getPartNumber(),
                record.getTemplate().getName(),
                record.getStartDate(),
                record.getExpectedDuration(),
                record.getStatus(),
                record.getNotes(),
                record.getAircraft().getRegistrationNumber().getNumber()
        );
    }
}
