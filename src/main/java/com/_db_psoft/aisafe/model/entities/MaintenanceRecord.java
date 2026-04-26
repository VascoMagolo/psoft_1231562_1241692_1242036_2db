package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.MaintenanceComponent;
import com._db_psoft.aisafe.model.enums.MaintenanceStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;


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
    @Column(nullable = false)
    private String notes;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceComponent component;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    protected MaintenanceRecord() {}

    public MaintenanceRecord(String description, LocalDateTime startDate, Integer expectedDuration,
                             MaintenanceComponent component) {

        if (expectedDuration == null || expectedDuration <= 0) {
            throw new IllegalArgumentException("Expected duration must be greater than zero.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or blank.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date must not be null.");
        }
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        this.description = description;
        this.startDate = startDate;
        this.expectedDuration = expectedDuration;
        this.component = component;
        this.notes = "";

        this.status = MaintenanceStatus.PLANNED;
    }
    public void startMaintenance() {
        this.status = MaintenanceStatus.IN_PROGRESS;
    }

    public void completeMaintenance(String notes) {
        this.status = MaintenanceStatus.COMPLETED;
        this.notes = notes;
    }

    public void cancelMaintenance(String reason) {
        this.status = MaintenanceStatus.CANCELED;
        this.notes = reason;
    }

}



