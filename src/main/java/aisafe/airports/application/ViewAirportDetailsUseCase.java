package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

/**
 * Use case for viewing the details of a specific airport.
 */
@UseCase
public class ViewAirportDetailsUseCase {
    private final AirportRepository airportRepository;

    public ViewAirportDetailsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Retrieves the details of an airport
     * @param iataCode the IATA code of the airport to retrieve
     * @return a DTO containing the details of the specified airport
     */
    public AirportResponse execute(String iataCode) {
        return AirportResponse.from(
                airportRepository.findByIataCodeCode(iataCode)
                        .orElseThrow(() -> new AirportNotFoundException(iataCode))
        );
    }
}
