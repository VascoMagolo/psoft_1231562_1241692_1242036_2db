package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.ViewAllMaintenanceRecordsResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public PaginatedResult<ViewAllMaintenanceRecordsResponse> execute(RegistrationNumber registrationNumber, int pageNumber, int pageSize) {
        aircraftRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number: " + registrationNumber.getNumber() + " not found."));

        PaginatedResult<MaintenanceRecord> recordsPage = repository.findByAircraftRegistration(
                registrationNumber.getNumber(), pageNumber, pageSize);

        List<ViewAllMaintenanceRecordsResponse> data = recordsPage.data().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PaginatedResult<>(data, recordsPage.totalElements());
    }

    private ViewAllMaintenanceRecordsResponse toResponse(MaintenanceRecord record) {
        return new ViewAllMaintenanceRecordsResponse(
                record.getPart().getPartNumber(),
                record.getTemplate().getName(),
                record.getStartDate(),
                record.getExpectedDuration(),
                record.getStatus(),
                record.getNotes(),
                record.getAircraftRegistration()
        );
    }
}
