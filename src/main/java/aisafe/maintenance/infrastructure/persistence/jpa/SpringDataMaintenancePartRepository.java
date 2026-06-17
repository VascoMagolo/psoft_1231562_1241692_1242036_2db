package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataMaintenancePartRepository extends JpaRepository<MaintenancePartJpaEntity, Long> {
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePartJpaEntity> findByPartNumber(String partNumber);

    @Query("SELECT p FROM MaintenancePartJpaEntity p WHERE " +
           "(:partNumber IS NULL OR LOWER(p.partNumber) LIKE LOWER(CONCAT('%', :partNumber, '%'))) AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:component IS NULL OR p.component = :component) AND " +
           "(:lowStockOnly = false OR p.stockQuantity < p.minimumThreshold)")
    Page<MaintenancePartJpaEntity> searchParts(
            @Param("partNumber") String partNumber,
            @Param("name") String name,
            @Param("component") MaintenanceComponent component,
            @Param("lowStockOnly") boolean lowStockOnly,
            Pageable pageable);
}
