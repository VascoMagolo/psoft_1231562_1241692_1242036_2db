package aisafe.maintenance.domain;

import java.util.Objects;
import java.util.List;

public class MaintenanceTemplate {
    private final String name;
    private final MaintenanceType templateType;
    private final List<String> applicableModelNames;
    private List<String> checklist;
    private Integer intervalFlightHours;
    private Integer intervalDays;

    public MaintenanceTemplate(String name, MaintenanceType templateType, List<String> applicableModelNames,
                               List<String> checklist, Integer intervalFlightHours, Integer intervalDays) {
        if (name == null) throw new MaintenanceInvalidFieldException("Name cannot be null");
        if (name.trim().isEmpty()) throw new MaintenanceInvalidFieldException("Name cannot be empty");
        if (templateType == null) throw new MaintenanceInvalidFieldException("Template cannot be null");
        if (applicableModelNames == null) throw new MaintenanceInvalidFieldException("Template must have applicable models");
        if (checklist == null) throw new MaintenanceInvalidFieldException("Template must have a checklist");
        if (intervalFlightHours == null) throw new MaintenanceInvalidFieldException("Template must have an interval in flight hours");
        if (intervalDays == null) throw new MaintenanceInvalidFieldException("Template must have an interval in days");
        this.name = name;
        this.templateType = templateType;
        this.applicableModelNames = applicableModelNames;
        this.checklist = checklist;
        this.intervalFlightHours = intervalFlightHours;
        this.intervalDays = intervalDays;
    }

    public String getName() { return name; }
    public MaintenanceType getTemplateType() { return templateType; }
    public List<String> getApplicableModelNames() { return applicableModelNames; }
    public List<String> getChecklist() { return checklist; }
    public Integer getIntervalFlightHours() { return intervalFlightHours; }
    public Integer getIntervalDays() { return intervalDays; }

    public void updateDetails(List<String> checklist, Integer intervalFlightHours, Integer intervalDays) {
        if (checklist != null && !checklist.isEmpty()) {
            this.checklist = checklist;
        }
        if (intervalFlightHours != null) {
            this.intervalFlightHours = intervalFlightHours;
        }
        if (intervalDays != null) {
            this.intervalDays = intervalDays;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintenanceTemplate that = (MaintenanceTemplate) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
