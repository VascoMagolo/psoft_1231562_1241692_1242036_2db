package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Searches aircraft by specific features (e.g., WiFi-enabled, specific engine type).
 */

@UseCase
@Transactional(readOnly = true)
public class SearchAircraftByFeatureUseCase {
    private final AircraftRepository aircraftRepository;

    public SearchAircraftByFeatureUseCase(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

        /**
        * Searches for aircraft that have the specified feature.
        * @param feature The feature to search for (e.g., "WiFi-enabled", engine-type).
        * @return A paginated list of aircraft that match the specified feature, along with the total count of matching aircraft.
         * @throws IllegalArgumentException if the feature is null or empty.
        */
    public PaginatedResult<SearchAircraftUseCaseResponse> execute(String feature, int pageNumber, int pageSize) {
        if (feature == null || feature.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        PaginatedResult<Aircraft> domainResult = aircraftRepository.searchAircraftByFeature(feature, pageNumber, pageSize);

        List<SearchAircraftUseCaseResponse> dtoList = domainResult.data().stream()
                .map(SearchAircraftUseCaseResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(dtoList, domainResult.totalElements());
    }
}
