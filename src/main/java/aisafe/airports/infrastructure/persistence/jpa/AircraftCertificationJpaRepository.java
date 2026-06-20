package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.ModelName;
import aisafe.airports.domain.AircraftCertification;
import aisafe.airports.domain.AircraftCertificationRepository;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.IataCode;
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
    public List<AircraftCertification> findByAirportCode(IataCode airportCode) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(new IataCodeJpaEmbeddable(airportCode.getCode()))
                .orElseThrow(() -> new AirportNotFoundException(airportCode.getCode()));

        return springRepo.findByAirport(jpaAirport).stream()
                .map(AircraftCertificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByAirportCodeAndAircraftModelName(IataCode airportCode, ModelName aircraftModelName) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(new IataCodeJpaEmbeddable(airportCode.getCode()))
                .orElse(null);
        if (jpaAirport == null) return false;
        return springRepo.existsByAirportAndAircraftModelName(jpaAirport, aircraftModelName.getName());
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
    public AircraftCertification save(AircraftCertification certification) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(
                        new IataCodeJpaEmbeddable(certification.getAirportCode().getCode()))
                .orElseThrow(() -> new AirportNotFoundException(
                        certification.getAirportCode().getCode()));

        springRepo.save(new AircraftCertificationJpaEntity(jpaAirport, certification.getAircraftModelName().getName()));
        return certification;
    }

    @Override
    public void delete(AircraftCertification certification) {
        AirportJpaEntity jpaAirport = airportSpringRepo.findByIataCode(
                        new IataCodeJpaEmbeddable(certification.getAirportCode().getCode()))
                .orElseThrow(() -> new AirportNotFoundException(
                        certification.getAirportCode().getCode()));

        springRepo.findByAirport(jpaAirport).stream()
                .filter(e -> e.getAircraftModelName().equals(certification.getAircraftModelName().getName()))
                .findFirst()
                .ifPresent(springRepo::delete);
    }
}
