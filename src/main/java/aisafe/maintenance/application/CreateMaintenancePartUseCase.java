package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartAlreadyExistsException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@Transactional
public class CreateMaintenancePartUseCase {
    private final MaintenancePartRepository maintenancePartRepository;

    public CreateMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
    }

    public MaintenancePart execute(CreateMaintenancePartRequest request){
        MaintenancePart part = new MaintenancePart(
                request.partNumber(),
                request.name(),
                request.description(),
                request.stockQuantity(),
                request.minimumThreshold(),
                request.component()
        );
        if (maintenancePartRepository.existsByPartNumber(part.getPartNumber())) {
            throw new MaintenancePartAlreadyExistsException("Part with the same part number already exists.");
        }
        return maintenancePartRepository.save(part);
    }
}
