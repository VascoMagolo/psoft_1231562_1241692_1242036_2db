package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@UseCase
public class SearchAirportUseCase {
    private final AirportRepository airportRepository;

    public SearchAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Page<AirportResponse> execute(String name, String city, String country, Pageable pageable) {
        return airportRepository.searchAirports(name, city, country, pageable)
                .map(AirportResponse::from);
    }
}
