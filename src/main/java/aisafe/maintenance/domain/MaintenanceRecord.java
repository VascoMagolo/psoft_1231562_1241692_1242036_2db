package aisafe.maintenance.domain;

import org.springframework.util.Assert;
import java.time.LocalDateTime;
import java.util.UUID;

public class MaintenanceRecord {
    private UUID recordId;
    private Long version;
    private String description;
    private LocalDateTime startDate;
    private Integer expectedDuration;
    private String notes;
    private MaintenancePart part;
    private MaintenanceTemplate template;
    private MaintenanceStatus status;
    private String aircraftRegistration;

    public MaintenanceRecord(String description, LocalDateTime startDate, Integer expectedDuration,
                             MaintenancePart part, String notes, MaintenanceTemplate template,
                             MaintenanceStatus status, String aircraftRegistration) {
        Assert.hasText(description, "Description must not be blank.");
        Assert.notNull(startDate, "Start date must not be null.");
        Assert.notNull(expectedDuration, "Expected duration must not be null.");
        Assert.isTrue(expectedDuration > 0, "Expected duration must be greater than zero.");
        Assert.notNull(part, "Maintenance part must not be null.");
        Assert.notNull(aircraftRegistration, "Record must have an aircraft registration number.");
        Assert.notNull(template, "Maintenance template must not be null.");
        Assert.notNull(status, "Maintenance status must not be null.");
        this.recordId = UUID.randomUUID();
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.part = part;
        this.notes = notes;
        this.template = template;
        this.status = status;
        this.aircraftRegistration = aircraftRegistration;
    }

    public UUID getRecordId() { return recordId; }
    public Long getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public Integer getExpectedDuration() { return expectedDuration; }
    public String getNotes() { return notes; }
    public MaintenancePart getPart() { return part; }
    public MaintenanceTemplate getTemplate() { return template; }
    public MaintenanceStatus getStatus() { return status; }
    public String getAircraftRegistration() { return aircraftRegistration; }

    public void setRecordId(UUID recordId) { this.recordId = recordId; }
    public void setVersion(Long version) { this.version = version; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
}
