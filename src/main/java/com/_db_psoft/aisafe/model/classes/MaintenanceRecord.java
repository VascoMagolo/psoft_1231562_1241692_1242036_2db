package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.MaintenanceComponent;

import java.util.Date;

public class MaintenanceRecord {
    private Integer id;
    public String description;
    public Date startDate;
    public Integer expectedDuration;
    public String notes;
    public MaintenanceComponent component;
    public Boolean isCompleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getExpectedDuration() {
        return expectedDuration;
    }

    public void setExpectedDuration(Integer expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public MaintenanceComponent getComponent() {
        return component;
    }

    public void setComponent(MaintenanceComponent component) {
        this.component = component;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }
}
