package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<MaintenanceRecordJpaEntity> findByStatusOrderByStartDateDesc(MaintenanceStatus status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM MaintenanceRecordJpaEntity m " +
           "INNER JOIN m.components c " +
           "WHERE (:registration IS NULL OR m.aircraftRegistration = :registration) " +
           "AND (:from IS NULL OR m.startDate >= :from) " +
           "AND (:to IS NULL OR m.startDate <= :to) " +
           "AND (:component IS NULL OR c = :component)")
    Page<MaintenanceRecordJpaEntity> search(
            @Param("registration") String registration,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("component") MaintenanceComponent component,
            Pageable pageable
    );
}
