package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.MaintenanceTemplate;
import aisafe.maintenance.domain.MaintenanceTemplateNotFoundException;
import aisafe.maintenance.domain.MaintenanceTemplateRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public class MaintenanceTemplateJpaRepository implements MaintenanceTemplateRepository {

    private final SpringDataMaintenanceTemplateRepository springRepo;

    public MaintenanceTemplateJpaRepository(SpringDataMaintenanceTemplateRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public boolean existsByName(String name) {
        return springRepo.existsByName(name);
    }

    @Override
    public Optional<MaintenanceTemplate> findByName(String name) {
        return springRepo.findByName(name).map(MaintenanceTemplateMapper::toDomain);
    }

    @Override
    public void save(MaintenanceTemplate template) {
        MaintenanceTemplateJpaEntity existing = springRepo.findByName(template.getName()).orElse(null);
        MaintenanceTemplateJpaEntity jpaEntity = MaintenanceTemplateMapper.toJpa(template);
        if (existing != null) {
            jpaEntity.setId(existing.getId());
        }
        springRepo.save(jpaEntity);
    }

    @Override
    public void delete(MaintenanceTemplate template) {
        MaintenanceTemplateJpaEntity jpaEntity = springRepo.findByName(template.getName())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template not found: " + template.getName()));
        springRepo.delete(jpaEntity);
    }
}
