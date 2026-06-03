package aisafe.airports.application;

import aisafe.shared.domain.DuplicateResourceException;
import aisafe.shared.application.UseCase;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.airports.application.dtos.AddCertificationRequest;
import aisafe.airports.application.dtos.AircraftCertificationResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.IataCode; // Importar o Value Object do IataCode
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;

/**
 * Use case for adding a new aircraft certification to an airport.
 */
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

        // 1. Usamos o Value Object IataCode e um nome de método puro
        IataCode iataCode = new IataCode(iataCodeStr);
        Airport airport = airportRepository.findByIataCodeCode(String.valueOf(iataCode))
                .orElseThrow(() -> new AirportNotFoundException(iataCodeStr));

        // 2. Proteção de Bounded Contexts: Verificamos apenas se existe,
        // sem instanciar a Entidade AircraftModel no módulo de Aeroportos!
        if (!aircraftModelRepository.existsByModelName(request.aircraftModelName())) {
            throw new AircraftModelNotFoundException("Aircraft model with name '" + request.aircraftModelName() + "' not found.");
        }

        // 3. Validação de Duplicados
        if (certificationRepository.existsByAirportAndAircraftModelName(airport, request.aircraftModelName())) {
            throw new DuplicateResourceException("Aircraft model '" + request.aircraftModelName() + "' is already certified for airport " + iataCodeStr + ".");
        }

        // 4. Criação e Persistência
        AircraftCertification certification = new AircraftCertification(airport, request.aircraftModelName());
        AircraftCertification saved = certificationRepository.save(certification);

        return AircraftCertificationResponse.from(saved);
    }
}