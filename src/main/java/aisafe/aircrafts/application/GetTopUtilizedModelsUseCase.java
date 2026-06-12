package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.TopUtilizedModelResponse;
import aisafe.aircrafts.infrastructure.persistence.TopUtilizedModelProjection;
import aisafe.routes.infrastructure.persistence.SpringDataScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets the Top 5 most utilized aircraft models based on total flight hours or number of assignments.
 */
@UseCase
@Transactional(readOnly = true)
public class GetTopUtilizedModelsUseCase {

    private final SpringDataScheduledFlightRepository repository;

    public GetTopUtilizedModelsUseCase(SpringDataScheduledFlightRepository repository) {
        this.repository = repository;
    }

    public List<TopUtilizedModelResponse> execute(String criteria) {
        List<TopUtilizedModelProjection> projections;
        if ("HOURS".equalsIgnoreCase(criteria)) {
            projections = repository.findTopModelsByFlightHours(PageRequest.of(0, 5));
        } else if ("ASSIGNMENTS".equalsIgnoreCase(criteria)) {
            projections = repository.findTopModelsByAssignments(PageRequest.of(0, 5));
        } else {
            throw new IllegalArgumentException("Invalid criteria. Must be 'HOURS' or 'ASSIGNMENTS'.");
        }

        return projections.stream()
                .map(p -> new TopUtilizedModelResponse(p.getModelName(), p.getUtilizationValue()))
                .collect(Collectors.toList());
    }
}
