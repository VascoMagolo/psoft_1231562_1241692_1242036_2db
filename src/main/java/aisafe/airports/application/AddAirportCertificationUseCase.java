package aisafe.airports.application;

import aisafe.DomainException;
import aisafe.UseCase;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.airports.application.dtos.AddCertificationRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirplaneCertification;
import aisafe.airports.domain.AirplaneCertificationRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

@UseCase
public class AddAirportCertificationUseCase {
    private final AirportRepository airportRepository;
    private final AirplaneCertificationRepository certificationRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public AddAirportCertificationUseCase(AirportRepository airportRepository,
                                          AirplaneCertificationRepository certificationRepository,
                                          AircraftModelRepository aircraftModelRepository) {
        this.airportRepository = airportRepository;
        this.certificationRepository = certificationRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    public AirplaneCertification execute(String iataCode, AddCertificationRequest request) {
        Airport airport = airportRepository.findByIataCodeCode(iataCode)
                .orElseThrow(() -> new AirportNotFoundException(iataCode));

        AircraftModel model = aircraftModelRepository.findById(request.aircraftModelId())
                .orElseThrow(() -> new DomainException("Aircraft model with id '" + request.aircraftModelId() + "' not found."));

        if (certificationRepository.existsByAirportAndAircraftModelId(airport, request.aircraftModelId())) {
            throw new DomainException("Aircraft model '" + model.getModelName() + "' is already certified for airport " + iataCode + ".");
        }

        return certificationRepository.save(new AirplaneCertification(airport, model));
    }
}
