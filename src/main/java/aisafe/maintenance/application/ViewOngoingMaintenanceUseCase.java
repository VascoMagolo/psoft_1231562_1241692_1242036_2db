package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class ViewOngoingMaintenanceUseCase {

    private final MaintenanceRecordRepository repository;

    public ViewOngoingMaintenanceUseCase(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    public PaginatedResult<MaintenanceRecordResponse> execute(int pageNumber, int pageSize) {
        PaginatedResult<MaintenanceRecord> result = repository.findByStatus(
                MaintenanceStatus.IN_PROGRESS, pageNumber, pageSize);

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
