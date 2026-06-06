package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for searching airports based on various criteria.
 */
@UseCase
@Transactional(readOnly = true)
public class SearchAirportUseCase {
    private final AirportRepository airportRepository;

    public SearchAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public PaginatedResult<AirportResponse> execute(String name, String city, String country, int pageNumber, int pageSize) {
        PaginatedResult<Airport> domainResult =
                airportRepository.searchAirports(name, city, country, pageNumber, pageSize);

        List<AirportResponse> dtos = domainResult.data().stream()
                .map(AirportResponse::from)
                .collect(Collectors.toList());

        return new PaginatedResult<>(dtos, domainResult.totalElements());
    }
}
