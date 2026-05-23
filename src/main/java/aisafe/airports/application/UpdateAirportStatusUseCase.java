package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatus;

/**
 * Use case for updating the details of an existing airport
 */
@UseCase
public class UpdateAirportStatusUseCase {
    private final AirportRepository airportRepository;

    public UpdateAirportStatusUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Updates the status of an existing airport.
     * @param iataCode the IATA code of the airport to update
     * @param status the new status to set for the airport
     * @return a DTO containing the updated details of the airport after the status change
     */
    public AirportResponse execute(String iataCode, AirportStatus status) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        airport.setStatus(status);
        return AirportResponse.from(airportRepository.save(airport));
    }
}
