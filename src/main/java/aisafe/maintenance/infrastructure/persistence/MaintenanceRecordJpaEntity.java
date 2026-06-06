package aisafe.maintenance.infrastructure.persistence;

import aisafe.maintenance.domain.MaintenanceStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_record")
public class MaintenanceRecordJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private Integer expectedDuration;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "part_id")
    private MaintenancePartJpaEntity part;

    @ManyToOne
    @JoinColumn(name = "template_maintenance_id")
    private MaintenanceTemplateJpaEntity template;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @Column(name = "aircraft_registration", nullable = false)
    private String aircraftRegistration;

    protected MaintenanceRecordJpaEntity() {}

    public MaintenanceRecordJpaEntity(String description, LocalDateTime startDate, Integer expectedDuration,
                                      String notes, MaintenancePartJpaEntity part,
                                      MaintenanceTemplateJpaEntity template, MaintenanceStatus status,
                                      String aircraftRegistration) {
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.notes = notes;
        this.part = part;
        this.template = template;
        this.status = status;
        this.aircraftRegistration = aircraftRegistration;
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public Integer getExpectedDuration() { return expectedDuration; }
    public String getNotes() { return notes; }
    public MaintenancePartJpaEntity getPart() { return part; }
    public MaintenanceTemplateJpaEntity getTemplate() { return template; }
    public MaintenanceStatus getStatus() { return status; }
    public String getAircraftRegistration() { return aircraftRegistration; }

    public void setId(Long id) { this.id = id; }
    public void setVersion(Long version) { this.version = version; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
}
