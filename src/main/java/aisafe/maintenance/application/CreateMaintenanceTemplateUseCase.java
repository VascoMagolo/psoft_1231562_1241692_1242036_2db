package aisafe.maintenance.application;

import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.maintenance.application.dtos.CreateMaintenanceTemplateRequest;
import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateAlreadyExistsException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a maintenance template with applicable maintenance tasks for respective model of aircraft
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

    public MaintenanceTemplate execute(CreateMaintenanceTemplateRequest request) {
        List<AircraftModel> models = request.applicableModels().stream()
                .map(modelName -> modelRepository.findByModelName(modelName)
                        .orElseThrow(() -> new AircraftModelNotFoundException("Model '" + modelName + "' not found.")))
                .collect(Collectors.toList());
        MaintenanceTemplate template = new MaintenanceTemplate(
            request.name(),
            request.templateType(),
            models,
            request.checklist(),
            request.intervalFlightHours(),
            request.intervalDays()
        );

        if(maintenanceTemplateRepository.existsByName(template.getName())){
            throw new MaintenanceTemplateAlreadyExistsException("A template with the name " + template.getName() + " already exists.");
        }

        return maintenanceTemplateRepository.save(template);
    }
}
