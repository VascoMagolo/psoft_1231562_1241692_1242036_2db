package aisafe.maintenance.domain;

import org.springframework.util.Assert;
import java.util.List;

public class MaintenanceTemplate {
    private String name;
    private MaintenanceType templateType;
    private List<String> applicableModelNames;
    private List<String> checklist;
    private Integer intervalFlightHours;
    private Integer intervalDays;

    public MaintenanceTemplate(String name, MaintenanceType templateType, List<String> applicableModelNames,
                               List<String> checklist, Integer intervalFlightHours, Integer intervalDays) {
        Assert.notNull(name, "Name cannot be null");
        Assert.notNull(templateType, "Template cannot be null");
        Assert.notNull(applicableModelNames, "Template must have applicable models");
        Assert.notNull(checklist, "Template must have a checklist");
        Assert.notNull(intervalFlightHours, "Template must have an interval in flight hours");
        Assert.notNull(intervalDays, "Template must have an interval in days");
        Assert.hasText(name, "Name cannot be empty");
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

}
