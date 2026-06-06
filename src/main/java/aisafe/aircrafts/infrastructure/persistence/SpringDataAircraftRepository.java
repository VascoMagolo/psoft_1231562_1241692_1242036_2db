package aisafe.aircrafts.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataAircraftRepository extends JpaRepository<AircraftJpaEntity, Long> {

    @Query("SELECT a FROM AircraftJpaEntity a WHERE " +
            "(:modelName IS NULL OR a.model.modelName = :modelName) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:year IS NULL OR YEAR(a.manufacturingDate) = :year)")
    Page<AircraftJpaEntity> searchAircrafts(@Param("modelName") String modelName,
                                            @Param("status") String status,
                                            @Param("year") Integer year,
                                            Pageable pageable);
    Optional<AircraftJpaEntity> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByModelModelName(String modelName);
}