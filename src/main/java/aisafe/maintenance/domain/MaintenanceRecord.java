package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.Aircraft;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Getter
@Entity
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false)
    private Integer expectedDuration;
    @Setter
    private String notes;
    @ManyToOne
    @JoinColumn(name = "part_id")
    private MaintenancePart part;
    @ManyToOne
    @JoinColumn(name = "template_maintenance_id")
    private MaintenanceTemplate template;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private MaintenanceStatus status;
    @ManyToOne
    @JoinColumn(name = "aircraft_id")
    private Aircraft aircraft;

    protected MaintenanceRecord() {}

    public MaintenanceRecord(String description, LocalDateTime startDate, Integer expectedDuration, MaintenancePart part, String notes, MaintenanceTemplate template, MaintenanceStatus status,Aircraft aircraft) {
        Assert.hasText(description, "Description must not be blank.");
        Assert.notNull(startDate, "Start date must not be null.");
        Assert.notNull(expectedDuration, "Expected duration must not be null.");
        Assert.isTrue(expectedDuration > 0, "Expected duration must be greater than zero.");
        Assert.notNull(part, "Maintenance part must not be null.");
        Assert.notNull(aircraft, "Record must have an aircraft");
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.part = part;
        this.notes = notes;
        this.template = template;
        this.status = status;
        this.aircraft = aircraft;
    }
}



