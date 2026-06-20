package aisafe.airports.application;

import aisafe.airports.domain.Airport;
import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;

/**
 * Use case for viewing the details of a specific airport.
 */
@UseCase(readOnly = true)
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
        IataCode code = new IataCode(iataCode);
        Airport airport = airportRepository.findByIataCode(code)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        Long version = airportRepository.findVersionFor(code);
        return AirportResponse.from(airport, version);
    }
}
