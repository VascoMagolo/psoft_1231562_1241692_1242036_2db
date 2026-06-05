package aisafe.maintenance.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataMaintenancePartRepository extends JpaRepository<MaintenancePartJpaEntity, Long> {
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePartJpaEntity> findByPartNumber(String partNumber);
}
