package aisafe.routes.infrastructure.persistence;

import aisafe.airports.domain.IataCode;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
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
        RouteJpaEntity jpaEntity = RouteMapper.toJpa(route);
        if (route.getId() != null) {
            jpaEntity.setId(route.getId());
            jpaEntity.setVersion(route.getVersion());
        }
        RouteJpaEntity saved = springRepo.save(jpaEntity);
        route.setId(saved.getId());
        route.setVersion(saved.getVersion());
        return route;
    }

    @Override
    public Optional<Route> findById(Long id) {
        return springRepo.findById(id).map(RouteMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return springRepo.existsById(id);
    }

    @Override
    public void delete(Route route) {
        springRepo.deleteById(route.getId());
    }

    @Override
    public List<Route> findAll() {
        return springRepo.findAll().stream()
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
        Page<RouteJpaEntity> page = springRepo.findByOriginCode(origin.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public PaginatedResult<Route> findByDestination(IataCode destination, int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findByDestinationCode(destination.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public PaginatedResult<Route> findByOriginAndDestination(IataCode origin, IataCode destination, int pageNumber, int pageSize) {
        Page<RouteJpaEntity> page = springRepo.findByOriginCodeAndDestinationCode(
                origin.getCode(), destination.getCode(), PageRequest.of(pageNumber, pageSize));
        return new PaginatedResult<>(page.map(RouteMapper::toDomain).toList(), page.getTotalElements());
    }

    @Override
    public boolean existsByOriginAndDestination(IataCode origin, IataCode destination) {
        return springRepo.existsByOriginCodeAndDestinationCode(origin.getCode(), destination.getCode());
    }

    @Override
    public List<Route> findByOriginOrDestination(IataCode origin, IataCode destination) {
        return springRepo.findByOriginCodeOrDestinationCode(origin.getCode(), destination.getCode())
                .stream().map(RouteMapper::toDomain).collect(Collectors.toList());
    }
}
