package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.application.dtos.UpdateMaintenancePartRequest;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.shared.application.UseCase;

@UseCase
public class UpdateMaintenancePartUseCase {

    private final MaintenancePartRepository maintenancePartRepository;

    public UpdateMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
    }

    public MaintenancePartResponse execute(String partNumber, UpdateMaintenancePartRequest request) {
        MaintenancePart part = maintenancePartRepository.findByPartNumber(partNumber)
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + partNumber));

        part.updateDetails(request.description(), request.stockQuantity(), request.minimumThreshold());

        maintenancePartRepository.save(part);

        return MaintenancePartResponse.from(part);
    }
}
