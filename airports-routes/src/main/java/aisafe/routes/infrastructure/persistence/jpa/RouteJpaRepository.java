package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.airports.domain.IataCode;
import aisafe.shared.infrastructure.messaging.EventPublisher;
import aisafe.routes.application.RouteDistanceService;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import aisafe.routes.infrastructure.readmodel.RouteProjection;
import aisafe.routes.infrastructure.readmodel.RouteView;
import aisafe.routes.infrastructure.replication.RouteReplicaHandler;
import aisafe.routes.infrastructure.replication.RouteReplicaSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class RouteJpaRepository implements RouteRepository {

    private final SpringDataRouteRepository springRepo;
    private final RouteReplicaSender replicaSender;
    private final EventPublisher events;
    private final RouteDistanceService distances;

    public RouteJpaRepository(SpringDataRouteRepository springRepo, RouteReplicaSender replicaSender,
                              EventPublisher events, RouteDistanceService distances) {
        this.springRepo = springRepo;
        this.replicaSender = replicaSender;
        this.events = events;
        this.distances = distances;
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
        RouteJpaEntity saved = springRepo.saveAndFlush(jpaEntity);
        replicaSender.routeSaved(saved);
        events.changed(RouteReplicaHandler.TYPE, RouteProjection.ENTITY, existing.isPresent() ? "updated" : "created", key(route),
                route.getStatus(), saved.getVersion(), RouteView.from(route, distances));
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
                .ifPresent(entity -> {
                    springRepo.delete(entity);
                    replicaSender.routeDeleted(route.getOrigin().getCode(), route.getDestination().getCode());
                    events.changed(RouteReplicaHandler.TYPE, RouteProjection.ENTITY, "deleted", key(route), null, null, null);
                });
    }

    @Override
    public List<Route> findAll() {
        return springRepo.findAll().stream()
                .map(RouteMapper::toDomain)
                .collect(Collectors.toList());
    }






    @Override
    public boolean existsByOriginAndDestination(IataCode origin, IataCode destination) {
        return springRepo.existsByOriginCode_CodeAndDestinationCode_Code(origin.getCode(), destination.getCode());
    }

    private static String key(Route route) {
        return route.getOrigin().getCode() + "-" + route.getDestination().getCode();
    }



}
