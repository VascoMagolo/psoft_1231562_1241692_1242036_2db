package aisafe.airports.infrastructure.persistence;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class AirportJpaRepository implements AirportRepository {

    private final SpringDataAirportRepository springRepo;

    public AirportJpaRepository(SpringDataAirportRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Optional<Airport> findByIataCodeCode(String code) {
        return springRepo.findByIataCode(code)
                .map(AirportMapper::toDomain);
    }

    @Override
    public boolean existsByIataCodeCode(String code) {
        return springRepo.existsByIataCode(code);
    }

    @Override
    public List<Airport> findAll() {
        return springRepo.findAll().stream()
                .map(AirportMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<Airport> searchAirports(String name, String city, String country, int pageNumber, int pageSize) {
        var pageable = PageRequest.of(pageNumber, pageSize);
        var jpaPage = springRepo.searchAirports(name, city, country, pageable);

        List<Airport> list = jpaPage.stream()
                .map(AirportMapper::toDomain)
                .collect(Collectors.toList());

        return new PaginatedResult<>(list, jpaPage.getTotalElements());
    }

    @Override
    public void save(Airport airport) {
        AirportJpaEntity existing = springRepo.findByIataCode(airport.getIataCode().getCode()).orElse(null);
        AirportJpaEntity jpaData = AirportMapper.toJpa(airport);

        if (existing != null) {
            jpaData.setId(existing.getId());
            jpaData.setVersion(existing.getVersion());
        }

        springRepo.save(jpaData);
    }

    @Override
    public void delete(Airport airport) {
        AirportJpaEntity jpaEntity = springRepo.findByIataCode(airport.getIataCode().getCode())
                .orElseThrow(() -> new AirportNotFoundException(airport.getIataCode().getCode()));
        springRepo.delete(jpaEntity);
    }
}
