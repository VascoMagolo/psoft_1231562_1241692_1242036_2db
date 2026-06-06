package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.maintenance.application.dtos.CreateMaintenancePartRequest;
import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartAlreadyExistsException;
import aisafe.maintenance.domain.MaintenancePartRepository;

/**
 * Use case for creating a new maintenance part in the system.
 */
@UseCase
public class CreateMaintenancePartUseCase {
    private final MaintenancePartRepository maintenancePartRepository;

    public CreateMaintenancePartUseCase(MaintenancePartRepository maintenancePartRepository) {
        this.maintenancePartRepository = maintenancePartRepository;
    }

    /**
     * Creates a new maintenance part based on the provided request data and saves it to the repository.
     * @param request the request containing the details of the maintenance part to be created
     * @return a response containing information of the created maintenance part
     */
    public MaintenancePartResponse execute(CreateMaintenancePartRequest request){
        MaintenancePart part = new MaintenancePart(
                request.partNumber(),
                request.name(),
                request.description(),
                request.stockQuantity(),
                request.minimumThreshold(),
                request.component()
        );

        if (maintenancePartRepository.existsByPartNumber(part.getPartNumber())) {
            throw new MaintenancePartAlreadyExistsException("Part with the same part number already exists: " + part.getPartNumber());
        }

        maintenancePartRepository.save(part);

        return new MaintenancePartResponse(
                part.getId(),
                part.getPartNumber(),
                part.getDescription()
        );
    }
}