package aisafe.airports.application;

import aisafe.aircrafts.domain.ModelName;
import aisafe.shared.domain.DuplicateResourceException;
import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.airports.application.dtos.AddCertificationRequest;
import aisafe.airports.application.dtos.AircraftCertificationResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;

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

    public AircraftCertificationResponse execute(String iataCodeStr, AddCertificationRequest request) {
        Airport airport = airportRepository.findByIataCode(new IataCode(iataCodeStr))
                .orElseThrow(() -> new AirportNotFoundException(iataCodeStr));

        ModelName modelName = new ModelName(request.aircraftModelName());

        if (!aircraftModelRepository.existsByModelName(modelName.getName())) {
            throw new AircraftModelNotFoundException("Aircraft model with name '" + modelName.getName() + "' not found.");
        }

        if (certificationRepository.existsByAirportAndAircraftModelName(airport, modelName)) {
            throw new DuplicateResourceException("Aircraft model '" + modelName.getName() + "' is already certified for airport " + iataCodeStr + ".");
        }

        AircraftCertification certification = new AircraftCertification(airport, modelName);
        certificationRepository.save(certification);

        return AircraftCertificationResponse.from(certification);
    }
}
