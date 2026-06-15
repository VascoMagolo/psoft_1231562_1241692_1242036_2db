package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatus;
import aisafe.airports.domain.IataCode;
import aisafe.shared.domain.ConcurrencyException;

import java.util.Objects;

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
     * @param clientVersion the version from the client's If-Match header
     * @return a DTO containing the updated details of the airport after the status change
     */
    public AirportResponse execute(String iataCode, AirportStatus status, Long clientVersion) {
        IataCode code = new IataCode(iataCode);
        Airport airport = airportRepository.findByIataCode(code)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));
        if (clientVersion != null) {
            Long current = airportRepository.findVersionFor(code);
            if (!Objects.equals(current, clientVersion)) {
                throw new ConcurrencyException("Airport version mismatch. Please fetch the latest version and retry.");
            }
        }
        airport.changeStatus(status);
        airportRepository.save(airport);
        Long newVersion = airportRepository.findVersionFor(code);
        return AirportResponse.from(airport, newVersion);
    }
}
