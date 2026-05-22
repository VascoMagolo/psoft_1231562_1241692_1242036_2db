package aisafe.maintenance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, Long> {
    boolean existsByName(String name);
    Optional<MaintenanceTemplate> findByName(String name);
}
