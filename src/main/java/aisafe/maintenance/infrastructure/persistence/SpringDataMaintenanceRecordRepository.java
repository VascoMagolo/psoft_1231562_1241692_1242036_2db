package aisafe.maintenance.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMaintenanceRecordRepository extends JpaRepository<MaintenanceRecordJpaEntity, Long> {
    boolean existsByStartDateAndTemplate(LocalDateTime startDate, MaintenanceTemplateJpaEntity template);
    boolean existsByPartsContaining(MaintenancePartJpaEntity part);
    boolean existsByTemplate(MaintenanceTemplateJpaEntity template);
    boolean existsByAircraftRegistration(String aircraftRegistration);
    Page<MaintenanceRecordJpaEntity> findByAircraftRegistration(String aircraftRegistration, Pageable pageable);
    Optional<MaintenanceRecordJpaEntity> findByRecordId(UUID recordId);
}
