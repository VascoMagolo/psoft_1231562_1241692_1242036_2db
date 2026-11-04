package aisafe.airports.infrastructure.persistence.jpa;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.IataCode;
import aisafe.airports.infrastructure.readmodel.AirportProjection;
import aisafe.airports.infrastructure.replication.AirportReplica;
import aisafe.airports.infrastructure.replication.AirportReplicaHandler;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.shared.domain.PaginatedResult;
import aisafe.shared.infrastructure.replication.Replicator;
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
    private final Replicator replicator;
    private final EventPublisher events;

    public AirportJpaRepository(SpringDataAirportRepository springRepo, Replicator replicator, EventPublisher events) {
        this.springRepo = springRepo;
        this.replicator = replicator;
        this.events = events;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Optional<Airport> findByIataCode(IataCode code) {
        return springRepo.findByIataCode(new IataCodeJpaEmbeddable(code.getCode()))
                .map(AirportMapper::toDomain);
    }

    @Override
    public boolean existsByIataCode(IataCode code) {
        return springRepo.existsByIataCode(new IataCodeJpaEmbeddable(code.getCode()));
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
    public List<AirportGroupingData> findAllGroupingByRegion() {
        return springRepo.findAllGroupingByRegion().stream()
                .map(p -> new AirportGroupingData(new IataCode(p.getIataCode()), p.getName(), p.getRegion(), p.getCountry()))
                .toList();
    }

    @Override
    public List<AirportGroupingData> findAllGroupingByCountry() {
        return springRepo.findAllGroupingByCountry().stream()
                .map(p -> new AirportGroupingData(new IataCode(p.getIataCode()), p.getName(), p.getRegion(), p.getCountry()))
                .toList();
    }

    @Override
    public Long findVersionFor(IataCode code) {
        return springRepo.findByIataCode(new IataCodeJpaEmbeddable(code.getCode()))
                .map(AirportJpaEntity::getVersion)
                .orElse(0L);
    }

    @Override
    public Airport save(Airport airport) {
        AirportJpaEntity existing = springRepo.findByIataCode(new IataCodeJpaEmbeddable(airport.getIataCode().getCode())).orElse(null);
        AirportJpaEntity jpaData = AirportMapper.toJpa(airport);

        if (existing != null) {
            jpaData.setId(existing.getId());
            jpaData.setVersion(existing.getVersion());
        }

        springRepo.saveAndFlush(jpaData);
        // Catalog: copied to every other replica
        replicator.catalogSaved(AirportReplicaHandler.TYPE, airport.getIataCode().getCode(), AirportReplica.from(airport));
        events.changed(AirportReplicaHandler.TYPE, AirportProjection.ENTITY, existing != null ? "updated" : "created",
                airport.getIataCode().getCode(), airport.getStatus(), null, AirportResponse.from(airport));
        return airport;
    }

    @Override
    public void delete(Airport airport) {
        AirportJpaEntity jpaEntity = springRepo.findByIataCode(new IataCodeJpaEmbeddable(airport.getIataCode().getCode()))
                .orElseThrow(() -> new AirportNotFoundException(airport.getIataCode().getCode()));
        springRepo.delete(jpaEntity);
        springRepo.flush();
        replicator.catalogDeleted(AirportReplicaHandler.TYPE, airport.getIataCode().getCode());
        events.changed(AirportReplicaHandler.TYPE, AirportProjection.ENTITY, "deleted", airport.getIataCode().getCode(), null, null, null);
    }
}
