package aisafe.airports.application;

import aisafe.shared.application.dtos.ImageData;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportPhotoNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.shared.application.UseCase;

@UseCase(readOnly = true)
public class GetAirportPhotoUseCase {

    private final AirportRepository airportRepository;

    public GetAirportPhotoUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public ImageData execute(String iataCode, int index) {
        Airport airport = airportRepository.findByIataCode(new IataCode(iataCode))
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        var photos = airport.getPhotos();
        if (photos.isEmpty() || index >= photos.size())
            throw new AirportPhotoNotFoundException(iataCode);

        return new ImageData(photos.get(index).getBytes(), photos.get(index).getContentType());
    }
}
