package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.maintenance.application.dtos.ViewAllMaintenanceRecordsResponse;
import aisafe.maintenance.application.dtos.ViewTotalMaintenanceHoursinFleetResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Views all maintenance hours from the whole fleet by joining all the maintenance record hours
 */
@UseCase
@Transactional(readOnly = true)
public class ViewTotalMaintenanceHoursInFleetUseCase {
    private final MaintenanceRecordRepository repository;

    public ViewTotalMaintenanceHoursInFleetUseCase(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    public ViewTotalMaintenanceHoursinFleetResponse execute() {
        Integer totalHours = repository.findAll().stream()
                .map(MaintenanceRecord::getExpectedDuration)
                .reduce(0, Integer::sum);

        return new ViewTotalMaintenanceHoursinFleetResponse(totalHours);
    }
}
