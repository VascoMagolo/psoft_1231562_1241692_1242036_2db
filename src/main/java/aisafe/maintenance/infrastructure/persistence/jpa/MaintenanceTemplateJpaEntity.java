package aisafe.maintenance.infrastructure.persistence.jpa;

import aisafe.maintenance.domain.MaintenanceType;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "maintenance_template")
public class MaintenanceTemplateJpaEntity {
    @Id
    @Column(name = "maintenance_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType templateType;

    @ElementCollection
    @CollectionTable(name = "maintenance_template_models", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "model_name")
    private List<String> applicableModelNames;

    @ElementCollection
    @Column(nullable = false)
    private List<String> checklist;

    @Column(nullable = false)
    private Integer intervalFlightHours;

    @Column(nullable = false)
    private Integer intervalDays;

    protected MaintenanceTemplateJpaEntity() {}

    public MaintenanceTemplateJpaEntity(String name, MaintenanceType templateType,
                                        List<String> applicableModelNames, List<String> checklist,
                                        Integer intervalFlightHours, Integer intervalDays) {
        this.name = name;
        this.templateType = templateType;
        this.applicableModelNames = applicableModelNames;
        this.checklist = checklist;
        this.intervalFlightHours = intervalFlightHours;
        this.intervalDays = intervalDays;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public MaintenanceType getTemplateType() { return templateType; }
    public List<String> getApplicableModelNames() { return applicableModelNames; }
    public List<String> getChecklist() { return checklist; }
    public Integer getIntervalFlightHours() { return intervalFlightHours; }
    public Integer getIntervalDays() { return intervalDays; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTemplateType(MaintenanceType templateType) { this.templateType = templateType; }
    public void setApplicableModelNames(List<String> applicableModelNames) { this.applicableModelNames = applicableModelNames; }
    public void setChecklist(List<String> checklist) { this.checklist = checklist; }
    public void setIntervalFlightHours(Integer intervalFlightHours) { this.intervalFlightHours = intervalFlightHours; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
}
