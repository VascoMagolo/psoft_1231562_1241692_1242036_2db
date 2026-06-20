package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.domain.RouteSummaryData;
import aisafe.routes.domain.RouteStatus;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class RouteJpaRepository implements RouteRepository {

    private final SpringDataRouteRepository springRepo;

    public RouteJpaRepository(SpringDataRouteRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Route save(Route route) {
        Optional<RouteJpaEntity> existing = springRepo.findByOriginCode_CodeAndDestinationCode_Code(
                route.getOrigin().getCode(), route.getDestination().getCode());
        RouteJpaEntity jpaEntity = RouteMapper.toJpa(route);
        existing.ifPresent(e -> {
            jpaEntity.setId(e.getId());
            jpaEntity.setVersion(e.getVersion());
        });
        springRepo.save(jpaEntity);
        return route;
    }

    @Override
    public Long findVersionFor(IataCode origin, IataCode destination) {
        return springRepo.findByOriginCode_CodeAndDestinationCode_Code(origin.getCode(), destination.getCode())
                .map(RouteJpaEntity::getVersion)
                .orElse(0L);
    }

    @Override
    public Optional<Route> findByOriginAndDestination(IataCode origin, IataCode destination) {
        return springRepo.findByOriginCode_CodeAndDestinationCode_Code(origin.getCode(), destination.getCode())
                .map(RouteMapper::toDomain);
    }

    @Override
    public void delete(Route route) {
        springRepo.findByOriginCode_CodeAndDestinationCode_Code(
                route.getOrigin().getCode(), route.getDestination().getCode())
                .ifPresent(springRepo::delete);
    }

    @Override
    public List<Route> findAll() {
        return springRepo.findAll().stream()
                .map(RouteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Route> findAllActive() {
        return springRepo.findByStatus(RouteStatus.ACTIVE).stream()
                .map(RouteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResult<Route> findAll(int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findAll(PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public PaginatedResult<Route> findByOrigin(IataCode origin, int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findByOriginCode_Code(origin.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public PaginatedResult<Route> findByDestination(IataCode destination, int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findByDestinationCode_Code(destination.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public PaginatedResult<Route> findByOriginAndDestination(IataCode origin, IataCode destination, int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findByOriginCode_CodeAndDestinationCode_Code(
                origin.getCode(), destination.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public boolean existsByOriginAndDestination(IataCode origin, IataCode destination) {
        return springRepo.existsByOriginCode_CodeAndDestinationCode_Code(origin.getCode(), destination.getCode());
    }

    @Override
    public List<Route> findByOriginOrDestination(IataCode origin, IataCode destination) {
        return springRepo.findByOriginCode_CodeOrDestinationCode_Code(origin.getCode(), destination.getCode())
                .stream().map(RouteMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Route> findCompatibleRoutes(Double range, Integer capacity) {
        return springRepo.findCompatibleRoutes(range, capacity).stream()
                .map(RouteMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteSummaryData> listSummariesForAirport(IataCode code) {
        return springRepo.findSummariesByAirportCode(code.getCode()).stream()
                .map(r -> new RouteSummaryData(
                        new IataCode(r.getOriginCode()),
                        new IataCode(r.getDestinationCode()),
                        r.getEstimatedFlightTime(),
                        r.getMinimumRange(),
                        r.getMinimumCapacity(),
                        r.getStatus(),
                        r.getVersion()
                ))
                .toList();
    }
}
