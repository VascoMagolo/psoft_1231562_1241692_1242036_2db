package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatus;

@UseCase
public class UpdateAirportStatusUseCase {
    private final AirportRepository airportRepository;

    public UpdateAirportStatusUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Airport execute(String iataCode, AirportStatus status) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        airport.setStatus(status);
        return airportRepository.save(airport);
    }
}
