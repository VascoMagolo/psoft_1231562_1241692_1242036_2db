package aisafe.maintenance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataMaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplateJpaEntity, Long> {
    boolean existsByName(String name);
    Optional<MaintenanceTemplateJpaEntity> findByName(String name);
}
