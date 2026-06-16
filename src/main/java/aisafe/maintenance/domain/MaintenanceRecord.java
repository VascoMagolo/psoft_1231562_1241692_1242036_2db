package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.RegistrationNumber;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MaintenanceRecord {
    private UUID recordId;
    private String description;
    private LocalDateTime startDate;
    private Integer expectedDuration;
    private String notes;
    private List<MaintenancePart> parts;
    private MaintenanceTemplate template;
    private MaintenanceStatus status;
    private RegistrationNumber aircraftRegistration;

    public MaintenanceRecord(UUID recordId, String description, LocalDateTime startDate, Integer expectedDuration,
                             List<MaintenancePart> parts, String notes, MaintenanceTemplate template,
                             MaintenanceStatus status, RegistrationNumber aircraftRegistration) {
        if (recordId == null) throw new MaintenanceInvalidFieldException("Record ID must not be null.");
        if (description == null || description.trim().isEmpty()) throw new MaintenanceInvalidFieldException("Description must not be blank.");
        if (startDate == null) throw new MaintenanceInvalidFieldException("Start date must not be null.");
        if (expectedDuration == null) throw new MaintenanceInvalidFieldException("Expected duration must not be null.");
        if (expectedDuration <= 0) throw new MaintenanceInvalidFieldException("Expected duration must be greater than zero.");
        if (parts == null) throw new MaintenanceInvalidFieldException("Maintenance parts must not be null.");
        if (parts.isEmpty()) throw new MaintenanceInvalidFieldException("Maintenance parts must not be empty.");
        if (aircraftRegistration == null) throw new MaintenanceInvalidFieldException("Record must have an aircraft registration number.");
        if (template == null) throw new MaintenanceInvalidFieldException("Maintenance template must not be null.");
        if (status == null) throw new MaintenanceInvalidFieldException("Maintenance status must not be null.");
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
    public String getDescription() { return description; }
    public LocalDateTime getStartDate() { return startDate; }
    public Integer getExpectedDuration() { return expectedDuration; }
    public String getNotes() { return notes; }
    public List<MaintenancePart> getParts() { return parts; }
    public MaintenanceTemplate getTemplate() { return template; }
    public MaintenanceStatus getStatus() { return status; }
    public RegistrationNumber getAircraftRegistration() { return aircraftRegistration; }

    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }
}
