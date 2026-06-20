package aisafe.maintenance.domain;

import aisafe.shared.domain.BaseRepository;

import java.util.Optional;

public interface MaintenanceTemplateRepository extends BaseRepository<MaintenanceTemplate> {
    boolean existsByName(String name);
    Optional<MaintenanceTemplate> findByName(String name);
}
