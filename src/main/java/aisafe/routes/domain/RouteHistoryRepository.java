package aisafe.routes.domain;

import aisafe.shared.domain.BaseRepository;

import java.util.List;

public interface RouteHistoryRepository extends BaseRepository<RouteHistory> {
    List<RouteHistory> findAllByRoute(String originCode, String destinationCode);
    void deleteAllByRoute(String originCode, String destinationCode);
}
