package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

@UseCase
public class ViewAirportDetailsUseCase {
    private final AirportRepository airportRepository;

    public ViewAirportDetailsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public AirportResponse execute(String iataCode) {
        return AirportResponse.from(
                airportRepository.findByIataCodeCode(iataCode)
                        .orElseThrow(() -> new AirportNotFoundException(iataCode))
        );
    }
}
