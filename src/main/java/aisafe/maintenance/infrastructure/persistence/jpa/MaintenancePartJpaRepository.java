package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class MaintenancePartJpaRepository implements MaintenancePartRepository {

    private final SpringDataMaintenancePartRepository springRepo;

    public MaintenancePartJpaRepository(SpringDataMaintenancePartRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public List<MaintenancePart> findAll() {
        return springRepo.findAll().stream()
                .map(MaintenancePartMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPartNumber(String partNumber) {
        return springRepo.existsByPartNumber(partNumber);
    }

    @Override
    public Optional<MaintenancePart> findByPartNumber(String partNumber) {
        return springRepo.findByPartNumber(partNumber).map(MaintenancePartMapper::toDomain);
    }

    @Override
    public void save(MaintenancePart part) {
        MaintenancePartJpaEntity existing = springRepo.findByPartNumber(part.getPartNumber()).orElse(null);
        MaintenancePartJpaEntity jpaEntity = MaintenancePartMapper.toJpa(part);
        if (existing != null) {
            jpaEntity.setId(existing.getId());
        }
        springRepo.save(jpaEntity);
    }

    @Override
    public void delete(MaintenancePart part) {
        MaintenancePartJpaEntity jpaEntity = springRepo.findByPartNumber(part.getPartNumber())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + part.getPartNumber()));
        springRepo.delete(jpaEntity);
    }
}
