package aisafe.maintenance.application;

import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.application.dtos.MaintenanceTemplateResponse;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateAlreadyExistsException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for creating a new maintenance template in the system.
 */
@UseCase
@Transactional
public class CreateMaintenanceTemplateUseCase {
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final AircraftModelRepository modelRepository;

    public CreateMaintenanceTemplateUseCase(MaintenanceTemplateRepository maintenanceTemplateRepository, AircraftModelRepository modelRepository) {
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
        this.modelRepository = modelRepository;
    }

    /**
     * Creates a new maintenance template based on the provided request data.
     * @param request the request containing the details of the maintenance template to be created
     * @return a response containing the details of the created maintenance template
     */
    public MaintenanceTemplateResponse execute(CreateMaintenanceTemplateRequest request) {
        List<AircraftModel> models = request.applicableModels().stream()
                .map(modelName -> modelRepository.findByModelName(modelName)
                        .orElseThrow(() -> new AircraftModelNotFoundException("Model '" + modelName + "' not found.")))
                .collect(Collectors.toList());

        MaintenanceTemplate template = new MaintenanceTemplate(
                request.name(),
                request.templateType(),
                request.applicableModels(),
                request.checklist(),
                request.intervalFlightHours(),
                request.intervalDays()
        );

        if(maintenanceTemplateRepository.existsByName(template.getName())){
            throw new MaintenanceTemplateAlreadyExistsException("A template with the name " + template.getName() + " already exists.");
        }

        MaintenanceTemplate savedTemplate = maintenanceTemplateRepository.save(template);

        return new MaintenanceTemplateResponse(
                savedTemplate.getId(),
                savedTemplate.getName(),
                savedTemplate.getTemplateType().name()
        );
    }
}