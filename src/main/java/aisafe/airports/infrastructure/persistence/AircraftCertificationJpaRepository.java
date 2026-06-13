package aisafe.airports.infrastructure.persistence;

import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class AircraftCertificationJpaRepository implements AircraftCertificationRepository {

    private final SpringDataAircraftCertificationRepository springRepo;
    private final SpringDataAirportRepository airportSpringRepo;

    public AircraftCertificationJpaRepository(SpringDataAircraftCertificationRepository springRepo,
                                               SpringDataAirportRepository airportSpringRepo) {
        this.springRepo = springRepo;
        this.airportSpringRepo = airportSpringRepo;
    }

    @Override
    public List<AircraftCertification> findByAirport(Airport airport) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(airport.getIataCode().getCode())
                .orElseThrow(() -> new AirportNotFoundException(airport.getIataCode().getCode()));

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
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<AircraftCertification> findAll() {
        return springRepo.findAll().stream()
                .map(AircraftCertificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(AircraftCertification certification) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(
                        certification.getAirport().getIataCode().getCode())
                .orElseThrow(() -> new AirportNotFoundException(
                        certification.getAirport().getIataCode().getCode()));

        springRepo.save(new AircraftCertificationJpaEntity(jpaAirport, certification.getAircraftModelName()));
    }

    @Override
    public void delete(AircraftCertification certification) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(
                        certification.getAirport().getIataCode().getCode())
                .orElseThrow(() -> new AirportNotFoundException(
                        certification.getAirport().getIataCode().getCode()));

        springRepo.findByAirport(jpaAirport).stream()
                .filter(e -> e.getAircraftModelName().equals(certification.getAircraftModelName()))
                .findFirst()
                .ifPresent(springRepo::delete);
    }
}
