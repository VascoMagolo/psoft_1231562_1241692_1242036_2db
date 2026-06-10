package aisafe.maintenance.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface SpringDataMaintenanceRecordRepository extends JpaRepository<MaintenanceRecordJpaEntity, Long> {
    boolean existsByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePartJpaEntity part, MaintenanceTemplateJpaEntity template);
    boolean existsByPart(MaintenancePartJpaEntity part);
    boolean existsByTemplate(MaintenanceTemplateJpaEntity template);
    boolean existsByAircraftRegistration(String aircraftRegistration);
    Page<MaintenanceRecordJpaEntity> findByAircraftRegistration(String aircraftRegistration, Pageable pageable);
}
