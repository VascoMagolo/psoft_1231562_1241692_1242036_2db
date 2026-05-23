package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.maintenance.application.dtos.ViewTotalMaintenanceHoursInFleetResponse;
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

    /**
     * Calculates the total maintenance hours for the entire fleet by summing up the expected duration of all maintenance records.
     * @return a response containing the total maintenance hours in the fleet
     */
    public ViewTotalMaintenanceHoursInFleetResponse execute() {
        Integer totalHours = repository.findAll().stream()
                .map(MaintenanceRecord::getExpectedDuration)
                .reduce(0, Integer::sum);

        return new ViewTotalMaintenanceHoursInFleetResponse(totalHours);
    }
}
