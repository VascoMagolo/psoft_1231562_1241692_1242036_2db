package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.maintenance.application.dtos.AverageTurnaroundByModelResponse;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.application.UseCase;

import java.util.List;

@UseCase(readOnly = true)
public class ViewAverageMaintenanceTurnaroundUseCase {

    private final MaintenanceRecordRepository recordRepository;
    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public ViewAverageMaintenanceTurnaroundUseCase(MaintenanceRecordRepository recordRepository,
                                                   AircraftRepository aircraftRepository,
                                                   AircraftModelRepository aircraftModelRepository) {
        this.recordRepository = recordRepository;
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public AverageTurnaroundByModelResponse execute(String modelName) {
        if (!aircraftModelRepository.existsByModelName(modelName)) {
            throw new AircraftModelNotFoundException("Aircraft model '" + modelName + "' not found.");
        }
        List<String> registrations = aircraftRepository.findAll().stream()
                .filter(a -> a.getModel().getModelName().equals(modelName))
                .map(a -> a.getRegistrationNumber().getNumber())
                .toList();
        var data = recordRepository.findAverageTurnaroundByRegistrations(modelName, registrations);
        return new AverageTurnaroundByModelResponse(data.modelName(), data.averageHours());
    }
}
