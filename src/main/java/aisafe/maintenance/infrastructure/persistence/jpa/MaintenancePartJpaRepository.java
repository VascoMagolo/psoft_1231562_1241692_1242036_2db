package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartNotFoundException;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public PaginatedResult<MaintenancePart> searchParts(String partNumber, String name, MaintenanceComponent component, Boolean lowStockOnly, int pageNumber, int pageSize) {
        Page<MaintenancePartJpaEntity> page = springRepo.searchParts(
                partNumber, name, component, lowStockOnly != null && lowStockOnly,
                PageRequest.of(pageNumber, pageSize)
        );

        List<MaintenancePart> data = page.getContent().stream()
                .map(MaintenancePartMapper::toDomain)
                .collect(Collectors.toList());

        return new PaginatedResult<>(data, page.getTotalElements());
    }

    @Override
    public MaintenancePart save(MaintenancePart part) {
        MaintenancePartJpaEntity existing = springRepo.findByPartNumber(part.getPartNumber()).orElse(null);
        MaintenancePartJpaEntity jpaEntity = MaintenancePartMapper.toJpa(part);
        if (existing != null) {
            jpaEntity.setId(existing.getId());
        }
        springRepo.save(jpaEntity);
        return part;
    }

    @Override
    public void delete(MaintenancePart part) {
        MaintenancePartJpaEntity jpaEntity = springRepo.findByPartNumber(part.getPartNumber())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + part.getPartNumber()));
        springRepo.delete(jpaEntity);
    }
}
