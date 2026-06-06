package aisafe.maintenance.domain;

import java.util.Optional;

public interface MaintenanceTemplateRepository {
    long count();
    boolean existsByName(String name);
    Optional<MaintenanceTemplate> findByName(String name);

    void save(MaintenanceTemplate template);
    void delete(MaintenanceTemplate template);
}
