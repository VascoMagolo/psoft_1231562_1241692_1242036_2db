package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case for searching airports based on various criteria.
 */
@UseCase
public class SearchAirportUseCase {
    private final AirportRepository airportRepository;

    public SearchAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Searches for airports based on the provided criteria. All parameters are optional and can be used in combination.
     * @param name the name of the airport to search for
     * @param city the city where the airport is located
     * @param country the country where the airport is located
     * @param pageable pagination information for the search results
     * @return a page of AirportResponse DTOs matching the search criteria
     */
    public Page<AirportResponse> execute(String name, String city, String country, Pageable pageable) {
        return airportRepository.searchAirports(name, city, country, pageable)
                .map(AirportResponse::from);
    }
}
