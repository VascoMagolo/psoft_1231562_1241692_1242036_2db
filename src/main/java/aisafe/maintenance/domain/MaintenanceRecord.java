package aisafe.maintenance.domain;

import org.springframework.util.Assert;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MaintenanceRecord {
    private UUID recordId;
    private Long version;
    private String description;
    private LocalDateTime startDate;
    private Integer expectedDuration;
    private String notes;
    private List<MaintenancePart> parts;
    private MaintenanceTemplate template;
    private MaintenanceStatus status;
    private String aircraftRegistration;

    public MaintenanceRecord(UUID recordId, String description, LocalDateTime startDate, Integer expectedDuration,
                             List<MaintenancePart> parts, String notes, MaintenanceTemplate template,
                             MaintenanceStatus status, String aircraftRegistration) {
        Assert.notNull(recordId, "Record ID must not be null.");
        Assert.hasText(description, "Description must not be blank.");
        Assert.notNull(startDate, "Start date must not be null.");
        Assert.notNull(expectedDuration, "Expected duration must not be null.");
        Assert.isTrue(expectedDuration > 0, "Expected duration must be greater than zero.");
        Assert.notNull(parts, "Maintenance parts must not be null.");
        Assert.isTrue(!parts.isEmpty(), "Maintenance parts must not be empty.");
        Assert.notNull(aircraftRegistration, "Record must have an aircraft registration number.");
        Assert.notNull(template, "Maintenance template must not be null.");
        Assert.notNull(status, "Maintenance status must not be null.");
        this.recordId = recordId;
        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.parts = parts;
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
    public List<MaintenancePart> getParts() { return parts; }
    public MaintenanceTemplate getTemplate() { return template; }
    public MaintenanceStatus getStatus() { return status; }
    public String getAircraftRegistration() { return aircraftRegistration; }

    public void setVersion(Long version) { this.version = version; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
}
