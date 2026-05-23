package aisafe.maintenance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing MaintenancePart entities in the maintenance domain.
 */
@Repository
public interface MaintenancePartRepository extends JpaRepository<MaintenancePart, Long>{
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePart> findByPartNumber(String partNumber);
}
