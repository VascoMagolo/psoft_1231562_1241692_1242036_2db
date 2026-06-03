package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.Aircraft;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * Represents a maintenance record for an aircraft
 */
@Getter
@Entity
public class MaintenanceRecord {
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
    @Column(name = "aircraft_registration", nullable = false)
    private String aircraftRegistration;

    protected MaintenanceRecord() {}

    public MaintenanceRecord(String description, LocalDateTime startDate, Integer expectedDuration, MaintenancePart part, String notes, MaintenanceTemplate template, MaintenanceStatus status,String aircraftRegistration) {
        Assert.hasText(description, "Description must not be blank.");
        Assert.notNull(startDate, "Start date must not be null.");
        Assert.notNull(expectedDuration, "Expected duration must not be null.");
        Assert.isTrue(expectedDuration > 0, "Expected duration must be greater than zero.");
        Assert.notNull(part, "Maintenance part must not be null.");
        Assert.notNull(aircraftRegistration, "Record must have an aircraft registration number.");
        Assert.notNull(template, "Maintenance template must not be null.");
        Assert.notNull(status, "Maintenance status must not be null.");
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.part = part;
        this.notes = notes;
        this.template = template;
        this.status = status;
        this.aircraftRegistration = aircraftRegistration;
    }

    public String getAircraftRegistration() {
        return this.aircraftRegistration;
    }
}



