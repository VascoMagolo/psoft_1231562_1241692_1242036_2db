package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportRepository;

import java.util.List;

@UseCase
public class SearchAirportUseCase {
    private final AirportRepository airportRepository;

    public SearchAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportResponse> execute(String name, String city, String country) {
        return airportRepository.searchAirports(name, city, country).stream()
                .map(AirportResponse::from)
                .toList();
    }
}
