package aisafe.routes.infrastructure.persistence;

import aisafe.routes.domain.RouteHistory;
import aisafe.routes.domain.RouteHistoryRepository;
import aisafe.routes.domain.RouteNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class RouteHistoryJpaRepository implements RouteHistoryRepository {

    private final SpringDataRouteHistoryRepository springRepo;
    private final SpringDataRouteRepository routeSpringRepo;

    public RouteHistoryJpaRepository(SpringDataRouteHistoryRepository springRepo,
                                      SpringDataRouteRepository routeSpringRepo) {
        this.springRepo = springRepo;
        this.routeSpringRepo = routeSpringRepo;
    }

    @Override
    public void save(RouteHistory history) {
        RouteJpaEntity routeJpa = routeSpringRepo.findById(history.getRouteId())
                .orElseThrow(() -> new RouteNotFoundException(history.getRouteId().toString()));
        RouteHistoryJpaEntity jpaEntity = RouteHistoryMapper.toJpa(history, routeJpa);
        RouteHistoryJpaEntity saved = springRepo.save(jpaEntity);
        history.setId(saved.getId());
    }

    @Override
    public List<RouteHistory> findAllByRouteId(Long routeId) {
        return springRepo.findAllByRouteId(routeId).stream()
                .map(RouteHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }
}
