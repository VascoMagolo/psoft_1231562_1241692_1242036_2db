package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportPhoto;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.UseCase;

@UseCase
public class UploadAirportPhotoUseCase {

    private final AirportRepository airportRepository;

    public UploadAirportPhotoUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public AirportResponse execute(String iataCode, byte[] bytes, String contentType) {
        IataCode code = new IataCode(iataCode);
        Airport airport = airportRepository.findByIataCode(code)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        airport.addPhoto(new AirportPhoto(bytes, contentType));
        airportRepository.save(airport);

        Long version = airportRepository.findVersionFor(code);
        return AirportResponse.from(airport, version);
    }
}
