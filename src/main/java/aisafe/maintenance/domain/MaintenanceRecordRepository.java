package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    boolean existsByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePart part, MaintenanceTemplate template);
    Optional<MaintenanceRecord> findByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePart part, MaintenanceTemplate template);
    // find maintenance record by the aircraft's registration number
    List<MaintenanceRecord> findByAircraftRegistrationNumber(RegistrationNumber aircraft_registrationNumber);
}
