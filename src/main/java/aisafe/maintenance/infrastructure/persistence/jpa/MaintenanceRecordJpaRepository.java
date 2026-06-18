package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.*;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public boolean existsByStartDateAndTemplate(LocalDateTime startDate, MaintenanceTemplate template) {
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(template.getName()).orElse(null);
        if (templateJpa == null) return false;
        return springRepo.existsByStartDateAndTemplate(startDate, templateJpa);
    }

    @Override
    public boolean existsByPartsContaining(MaintenancePart part) {
        MaintenancePartJpaEntity partJpa = partSpringRepo.findByPartNumber(part.getPartNumber()).orElse(null);
        if (partJpa == null) return false;
        return springRepo.existsByPartsContaining(partJpa);
    }

    @Override
    public boolean existsByTemplate(MaintenanceTemplate template) {
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(template.getName()).orElse(null);
        if (templateJpa == null) return false;
        return springRepo.existsByTemplate(templateJpa);
    }

    @Override
    public boolean existsByAircraftRegistration(RegistrationNumber registrationNumber) {
        return springRepo.existsByAircraftRegistration(registrationNumber.getNumber());
    }

    @Override
    public List<MaintenanceRecord> findCompletedByAircraft(RegistrationNumber registrationNumber) {
        return springRepo.findByAircraftRegistrationAndStatusOrderByCompletedAtDesc(
                registrationNumber.getNumber(), MaintenanceStatus.COMPLETED).stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
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
    public Long findVersionFor(UUID recordId) {
        return springRepo.findVersionFor(recordId);
    }

    @Override
    public Long sumTotalMaintenanceHours() {
        Long sum = springRepo.sumTotalExpectedDuration();
        return sum != null ? sum : 0L;
    }

    @Override
    public PaginatedResult<MaintenanceRecord> search(RegistrationNumber aircraftRegistration, LocalDateTime from,
                                                     LocalDateTime to, MaintenanceComponent component,
                                                     int pageNumber, int pageSize) {
        Page<MaintenanceRecordJpaEntity> page = springRepo.search(
                aircraftRegistration != null ? aircraftRegistration.getNumber() : null,
                from, to, component, PageRequest.of(pageNumber, pageSize));
        List<MaintenanceRecord> data = page.stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
        return new PaginatedResult<>(data, page.getTotalElements());
    }

    @Override
    public PaginatedResult<MaintenanceRecord> findByStatus(MaintenanceStatus status, int pageNumber, int pageSize) {
        Page<MaintenanceRecordJpaEntity> page = springRepo.findByStatusOrderByStartDateDesc(
                status, PageRequest.of(pageNumber, pageSize));
        List<MaintenanceRecord> data = page.stream()
                .map(MaintenanceRecordMapper::toDomain)
                .collect(Collectors.toList());
        return new PaginatedResult<>(data, page.getTotalElements());
    }

    @Override
    public void save(MaintenanceRecord record) {
        List<MaintenancePartJpaEntity> partsJpa = record.getParts().stream()
                .map(p -> partSpringRepo.findByPartNumber(p.getPartNumber())
                        .orElseThrow(() -> new MaintenancePartNotFoundException("Part not found: " + p.getPartNumber())))
                .collect(Collectors.toList());
        MaintenanceTemplateJpaEntity templateJpa = templateSpringRepo.findByName(record.getTemplate().getName())
                .orElseThrow(() -> new MaintenanceTemplateNotFoundException("Template not found: " + record.getTemplate().getName()));

        MaintenanceRecordJpaEntity existing = springRepo.findByRecordId(record.getRecordId()).orElse(null);

        if (existing != null) {
            existing.setStatus(record.getStatus());
            existing.setNotes(record.getNotes());
            existing.setCompletedAt(record.getCompletedAt());
            springRepo.save(existing);
        } else {
            MaintenanceRecordJpaEntity jpaEntity = new MaintenanceRecordJpaEntity(
                    record.getRecordId(), record.getDescription(), record.getStartDate(), record.getExpectedDuration(),
                    record.getNotes(), partsJpa, templateJpa, record.getStatus(),
                    new HashSet<>(record.getComponents()), record.getAircraftRegistration().getNumber(),
                    record.getCost());
            jpaEntity.setCompletedAt(record.getCompletedAt());
            springRepo.save(jpaEntity);
        }
    }

    @Override
    public void delete(MaintenanceRecord record) {
        springRepo.findByRecordId(record.getRecordId()).ifPresent(springRepo::delete);
    }

    @Override
    public BigDecimal sumCostByAircraftRegistration(RegistrationNumber registration) {
        BigDecimal sum = springRepo.sumCostByAircraftRegistration(registration.getNumber());
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumCostByRegistrations(List<String> registrationNumbers) {
        if (registrationNumbers.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = springRepo.sumCostByRegistrations(registrationNumbers);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public MaintenanceTurnaroundData findAverageTurnaroundByRegistrations(String modelName, List<String> registrations) {
        if (registrations.isEmpty()) return new MaintenanceTurnaroundData(modelName, 0.0);
        Double avg = springRepo.findAverageTurnaroundHours(registrations);
        return new MaintenanceTurnaroundData(modelName, avg != null ? avg : 0.0);
    }
}
