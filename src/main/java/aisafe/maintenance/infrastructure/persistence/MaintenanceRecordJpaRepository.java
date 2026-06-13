package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.*;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("jpa")
public class MaintenanceRecordJpaRepository implements MaintenanceRecordRepository {

    private final SpringDataMaintenanceRecordRepository springRepo;
    private final SpringDataMaintenancePartRepository partSpringRepo;
    private final SpringDataMaintenanceTemplateRepository templateSpringRepo;

    public MaintenanceRecordJpaRepository(SpringDataMaintenanceRecordRepository springRepo,
                                          SpringDataMaintenancePartRepository partSpringRepo,
                                          SpringDataMaintenanceTemplateRepository templateSpringRepo) {
        this.springRepo = springRepo;
        this.partSpringRepo = partSpringRepo;
        this.templateSpringRepo = templateSpringRepo;
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public boolean existsByStartDateAndPartAndTemplate(LocalDateTime startDate, MaintenancePart part, MaintenanceTemplate template) {
        MaintenancePartJpaEntity partJpa = partSpringRepo.findByPartNumber(part.getPartNumber()).orElse(null);
        if (partJpa == null) return false;
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(template.getName()).orElse(null);
        if (templateJpa == null) return false;
        return springRepo.existsByStartDateAndPartAndTemplate(startDate, partJpa, templateJpa);
    }

    @Override
    public boolean existsByPart(MaintenancePart part) {
        MaintenancePartJpaEntity partJpa = partSpringRepo.findByPartNumber(part.getPartNumber()).orElse(null);
        if (partJpa == null) return false;
        return springRepo.existsByPart(partJpa);
    }

    @Override
    public boolean existsByTemplate(MaintenanceTemplate template) {
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(template.getName()).orElse(null);
        if (templateJpa == null) return false;
        return springRepo.existsByTemplate(templateJpa);
    }

    @Override
    public boolean existsByAircraftRegistration(String aircraftRegistration) {
        return springRepo.existsByAircraftRegistration(aircraftRegistration);
    }

    @Override
    public PaginatedResult<MaintenanceRecord> findByAircraftRegistration(String aircraftRegistration, int pageNumber, int pageSize) {
        Page<MaintenanceRecordJpaEntity> page = springRepo.findByAircraftRegistration(
                aircraftRegistration, PageRequest.of(pageNumber, pageSize));
        List<MaintenanceRecord> data = page.stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
        return new PaginatedResult<>(data, page.getTotalElements());
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return springRepo.findAll().stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MaintenanceRecord> findByRecordId(UUID recordId) {
        return springRepo.findByRecordId(recordId).map(MaintenanceRecordMapper::toDomain);
    }

    @Override
    public void save(MaintenanceRecord record) {
        MaintenancePartJpaEntity partJpa = partSpringRepo.findByPartNumber(record.getPart().getPartNumber())
                .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + record.getPart().getPartNumber()));
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(record.getTemplate().getName())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template not found: " + record.getTemplate().getName()));

        MaintenanceRecordJpaEntity existing = springRepo.findByRecordId(record.getRecordId()).orElse(null);

        if (existing != null) {
            existing.setStatus(record.getStatus());
            existing.setNotes(record.getNotes());
            existing.setVersion(record.getVersion());
            MaintenanceRecordJpaEntity saved = springRepo.save(existing);
            record.setVersion(saved.getVersion());
        } else {
            MaintenanceRecordJpaEntity jpaEntity = new MaintenanceRecordJpaEntity(
                    record.getRecordId(), record.getDescription(), record.getStartDate(), record.getExpectedDuration(),
                    record.getNotes(), partJpa, templateJpa, record.getStatus(), record.getAircraftRegistration());
            MaintenanceRecordJpaEntity saved = springRepo.save(jpaEntity);
            record.setVersion(saved.getVersion());
        }
    }

    @Override
    public void delete(MaintenanceRecord record) {
        springRepo.findByRecordId(record.getRecordId()).ifPresent(springRepo::delete);
    }
}
