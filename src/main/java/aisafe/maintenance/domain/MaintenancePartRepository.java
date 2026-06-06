package aisafe.maintenance.domain;

import java.util.Optional;

public interface MaintenancePartRepository {
    long count();
    boolean existsByPartNumber(String partNumber);
    Optional<MaintenancePart> findByPartNumber(String partNumber);

    void save(MaintenancePart part);
    void delete(MaintenancePart part);
}
