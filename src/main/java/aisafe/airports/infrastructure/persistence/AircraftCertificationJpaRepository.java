package aisafe.airports.infrastructure.persistence;

import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.Airport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class AircraftCertificationJpaRepository implements AircraftCertificationRepository {

    private final SpringDataAircraftCertificationRepository springRepo;
    private final SpringDataAirportRepository airportSpringRepo;

    public AircraftCertificationRepositoryImpl(SpringDataAircraftCertificationRepository springRepo,
                                               SpringDataAirportRepository airportSpringRepo) {
        this.springRepo = springRepo;
        this.airportSpringRepo = airportSpringRepo;
    }

    @Override
    public List<AircraftCertification> findByAirport(Airport airport) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(airport.getIataCode().getCode())
                .orElseThrow(() -> new aisafe.airports.domain.AirportNotFoundException(airport.getIataCode().getCode()));

        return springRepo.findByAirport(jpaAirport).stream()
                .map(AircraftCertificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByAirportAndAircraftModelName(Airport airport, String aircraftModelName) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(airport.getIataCode().getCode())
                .orElse(null);
        if (jpaAirport == null) return false;
        return springRepo.existsByAirportAndAircraftModelName(jpaAirport, aircraftModelName);
    }

    @Override
    public void save(AircraftCertification certification) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(
                        certification.getAirport().getIataCode().getCode())
                .orElseThrow(() -> new aisafe.airports.domain.AirportNotFoundException(
                        certification.getAirport().getIataCode().getCode()));

        springRepo.save(new AircraftCertificationJpaEntity(jpaAirport, certification.getAircraftModelName()));
    }
}
