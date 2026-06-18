package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "maintenance_record")
public class MaintenanceRecordJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID recordId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private Integer expectedDuration;

    private String notes;

    @ManyToMany
    @BatchSize(size = 20)
    @JoinTable(
        name = "maintenance_record_parts",
        joinColumns = @JoinColumn(name = "record_id"),
        inverseJoinColumns = @JoinColumn(name = "part_id")
    )
    private List<MaintenancePartJpaEntity> parts;

    @ManyToOne
    @JoinColumn(name = "template_maintenance_id")
    private MaintenanceTemplateJpaEntity template;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "maintenance_record_components", joinColumns = @JoinColumn(name = "maintenance_record_id"))
    @Column(name = "component", nullable = false)
    @BatchSize(size = 25)
    private Set<MaintenanceComponent> components = new HashSet<>();

    @Column(name = "aircraft_registration", nullable = false)
    private String aircraftRegistration;

    @Column(name = "cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected MaintenanceRecordJpaEntity() {}

    public MaintenanceRecordJpaEntity(UUID recordId, String description, LocalDateTime startDate, Integer expectedDuration,
                                      String notes, List<MaintenancePartJpaEntity> parts,
                                      MaintenanceTemplateJpaEntity template, MaintenanceStatus status,
                                      Set<MaintenanceComponent> components, String aircraftRegistration,
                                      BigDecimal cost) {
        this.recordId = recordId;
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.notes = notes;
        this.parts = parts;
        this.template = template;
        this.status = status;
        this.components = components;
        this.aircraftRegistration = aircraftRegistration;
        this.cost = cost;
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public UUID getRecordId() { return recordId; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public Integer getExpectedDuration() { return expectedDuration; }
    public String getNotes() { return notes; }
    public List<MaintenancePartJpaEntity> getParts() { return parts; }
    public MaintenanceTemplateJpaEntity getTemplate() { return template; }
    public MaintenanceStatus getStatus() { return status; }
    public Set<MaintenanceComponent> getComponents() { return components; }
    public String getAircraftRegistration() { return aircraftRegistration; }
    public BigDecimal getCost() { return cost; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setId(Long id) { this.id = id; }
    public void setVersion(Long version) { this.version = version; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
