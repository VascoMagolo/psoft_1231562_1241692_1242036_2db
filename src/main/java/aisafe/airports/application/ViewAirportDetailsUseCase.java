package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

@UseCase
public class ViewAirportDetailsUseCase {
    private final AirportRepository airportRepository;

    public ViewAirportDetailsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Airport execute(String iataCode) {
        return airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
    }
}
