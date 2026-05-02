package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;

import java.util.List;

@UseCase
public class SearchAirportUseCase {
    private final AirportRepository airportRepository;

    public SearchAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<Airport> execute(String name, String city, String country) {
        return airportRepository.searchAirports(name, city, country);
    }
}
