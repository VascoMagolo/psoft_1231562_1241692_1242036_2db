package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.ViewAllMaintenanceRecordsResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Retrieves a paginated list of maintenance records for the specified aircraft registration number.
     * @param registrationNumber the registration number of the aircraft for which to retrieve maintenance records
     * @param pageable the pagination information for the query
     * @return a paginated list of maintenance records for the specified aircraft
     */
    public Page<ViewAllMaintenanceRecordsResponse> execute(RegistrationNumber registrationNumber, Pageable pageable) {
        aircraftRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft with registration number: " + registrationNumber.getNumber() + " not found."));

        Page<MaintenanceRecord> recordsPage = repository.findByAircraftRegistrationNumber(registrationNumber, pageable);

        return recordsPage.map(this::toResponse);
    }

    /**
     * Converts a MaintenanceRecord entity to a ViewAllMaintenanceRecordsResponse DTO.
     * @param record the MaintenanceRecord entity to be converted
     * @return a ViewAllMaintenanceRecordsResponse DTO containing the details of the maintenance record
     */
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