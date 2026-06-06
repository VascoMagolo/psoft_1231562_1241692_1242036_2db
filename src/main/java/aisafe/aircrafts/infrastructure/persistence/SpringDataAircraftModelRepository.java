package aisafe.aircrafts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataAircraftModelRepository extends JpaRepository<AircraftModelJpaEntity, Long> {

    Optional<AircraftModelJpaEntity> findByModelName(String modelName);
    boolean existsByModelName(String modelName);
    void deleteByModelName(String modelName);
}