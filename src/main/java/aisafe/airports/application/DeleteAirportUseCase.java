package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

@UseCase
@Transactional
public class DeleteAirportUseCase {
    private final AirportRepository airportRepository;

    public DeleteAirportUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public void execute(String iataCode) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        airportRepository.delete(airport);
    }
}
