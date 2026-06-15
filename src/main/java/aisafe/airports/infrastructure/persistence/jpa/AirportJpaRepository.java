package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatisticsData;
import aisafe.airports.domain.IataCode;
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
    public Optional<Airport> findByIataCode(IataCode code) {
        return springRepo.findByIataCode(code.getCode())
                .map(AirportMapper::toDomain);
    }

    @Override
    public boolean existsByIataCode(IataCode code) {
        return springRepo.existsByIataCode(code.getCode());
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
    public List<AirportStatisticsData> findStatistics() {
        return springRepo.findStatistics().stream()
                .map(r -> new AirportStatisticsData(r.getIataCode(), r.getName(), r.getCity(), r.getCountry(), r.getRouteCount()))
                .toList();
    }

    @Override
    public List<Airport> findAllOrderedByRegion() {
        return springRepo.findAllOrderedByRegion().stream()
                .map(AirportMapper::toDomain)
                .toList();
    }

    @Override
    public List<Airport> findAllOrderedByCountry() {
        return springRepo.findAllOrderedByCountry().stream()
                .map(AirportMapper::toDomain)
                .toList();
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
