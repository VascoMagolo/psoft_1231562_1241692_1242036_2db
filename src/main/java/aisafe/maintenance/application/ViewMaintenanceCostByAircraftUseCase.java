package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.MaintenanceCostByAircraftResponse;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.application.UseCase;

import java.math.BigDecimal;

@UseCase(readOnly = true)
public class ViewMaintenanceCostByAircraftUseCase {

    private final MaintenanceRecordRepository recordRepository;
    private final AircraftRepository aircraftRepository;

    public ViewMaintenanceCostByAircraftUseCase(MaintenanceRecordRepository recordRepository,
                                                AircraftRepository aircraftRepository) {
        this.recordRepository = recordRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public MaintenanceCostByAircraftResponse execute(String registrationNumber) {
        RegistrationNumber reg = new RegistrationNumber(registrationNumber);
        if (!aircraftRepository.existsByRegistrationNumber(reg)) {
            throw new AircraftNotFoundException("Aircraft '" + registrationNumber + "' not found.");
        }
        BigDecimal total = recordRepository.sumCostByAircraftRegistration(reg);
        return new MaintenanceCostByAircraftResponse(registrationNumber, total);
    }
}
