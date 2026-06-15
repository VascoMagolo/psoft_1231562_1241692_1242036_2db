package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.TopUtilizedModelResponse;
import aisafe.flights.domain.ModelUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets the Top 5 most utilized aircraft models based on total flight hours or number of assignments.
 */
@UseCase(readOnly = true)
public class GetTopUtilizedModelsUseCase {

    private final ScheduledFlightRepository repository;

    public GetTopUtilizedModelsUseCase(ScheduledFlightRepository repository) {
        this.repository = repository;
    }

    public List<TopUtilizedModelResponse> execute(String criteria) {
        List<ModelUtilizationData> data;
        if ("HOURS".equalsIgnoreCase(criteria)) {
            data = repository.findTopModelsByFlightHours(5);
        } else if ("ASSIGNMENTS".equalsIgnoreCase(criteria)) {
            data = repository.findTopModelsByAssignments(5);
        } else {
            throw new InvalidUtilizationCriteriaException("Invalid criteria. Must be 'HOURS' or 'ASSIGNMENTS'.");
        }

        return data.stream()
                .map(d -> new TopUtilizedModelResponse(d.modelName(), d.utilizationValue()))
                .collect(Collectors.toList());
    }
}
