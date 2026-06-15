package aisafe.maintenance.domain;

import aisafe.shared.domain.BaseRepository;

import java.util.Optional;

public interface MaintenancePartRepository extends BaseRepository<MaintenancePart> {
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePart> findByPartNumber(String partNumber);
}
