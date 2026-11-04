package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.SearchAirportRequest;
import aisafe.shared.domain.PaginatedResult;


/**
 * Use case for searching airports based on various criteria.
 */
@UseCase(readOnly = true)
public class SearchAirportUseCase {
    private final AirportListings airports;

    public SearchAirportUseCase(AirportListings airports) {
        this.airports = airports;
    }

    public PaginatedResult<AirportResponse> execute(SearchAirportRequest request) {
        return airports.search(request.name(), request.city(), request.country(), request.pageNumber(), request.pageSize());
    }
}
