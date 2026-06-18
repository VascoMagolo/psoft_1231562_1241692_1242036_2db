package aisafe.maintenance.application;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class SearchMaintenanceRecordsUseCase {

    private final MaintenanceRecordRepository repository;

    public SearchMaintenanceRecordsUseCase(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    public PaginatedResult<MaintenanceRecordResponse> execute(
            RegistrationNumber aircraftRegistration,
            LocalDateTime from,
            LocalDateTime to,
            MaintenanceComponent component,
            int pageNumber,
            int pageSize) {

        PaginatedResult<MaintenanceRecord> result = repository.search(
                aircraftRegistration, from, to, component, pageNumber, pageSize);

        List<MaintenanceRecordResponse> data = result.data().stream()
                .map(r -> new MaintenanceRecordResponse(
                        r.getRecordId(),
                        r.getDescription(),
                        r.getStartDate(),
                        r.getExpectedDuration(),
                        r.getNotes(),
                        r.getParts().stream().map(p -> p.getPartNumber()).toList(),
                        r.getTemplate().getName(),
                        r.getStatus().name(),
                        r.getAircraftRegistration().getNumber(),
                        repository.findVersionFor(r.getRecordId()),
                        r.getComponents().stream().map(Enum::name).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());

        return new PaginatedResult<>(data, result.totalElements());
    }
}
