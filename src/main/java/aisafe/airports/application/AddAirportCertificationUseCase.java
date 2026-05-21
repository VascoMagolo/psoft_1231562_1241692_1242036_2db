package aisafe.airports.application;

import aisafe.DuplicateResourceException;
import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.airports.application.dtos.AddCertificationRequest;
import aisafe.airports.application.dtos.AircraftCertificationResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

@UseCase
public class AddAirportCertificationUseCase {
    private final AirportRepository airportRepository;
    private final AircraftCertificationRepository certificationRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public AddAirportCertificationUseCase(AirportRepository airportRepository,
                                          AircraftCertificationRepository certificationRepository,
                                          AircraftModelRepository aircraftModelRepository) {
        this.airportRepository = airportRepository;
        this.certificationRepository = certificationRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public AircraftCertificationResponse execute(String iataCode, AddCertificationRequest request) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        AircraftModel model = aircraftModelRepository.findById(request.aircraftModelId())
                .orElseThrow(() -> new AircraftNotFoundException("Aircraft model with id '" + request.aircraftModelId() + "' not found."));

        if (certificationRepository.existsByAirportAndAircraftModelId(airport, request.aircraftModelId())) {
            throw new DuplicateResourceException("Aircraft model '" + model.getModelName() + "' is already certified for airport " + iataCode + ".");
        }

        return AircraftCertificationResponse.from(certificationRepository.save(new AircraftCertification(airport, model)));
    }
}
