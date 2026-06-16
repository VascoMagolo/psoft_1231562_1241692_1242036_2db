package aisafe.maintenance.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT m.version FROM MaintenanceRecordJpaEntity m WHERE m.recordId = :recordId")
    Long findVersionFor(UUID recordId);

    @Query("SELECT SUM(m.expectedDuration) FROM MaintenanceRecordJpaEntity m")
    Long sumTotalExpectedDuration();
}
