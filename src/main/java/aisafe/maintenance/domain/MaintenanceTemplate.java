package aisafe.maintenance.domain;

import aisafe.aircrafts.domain.AircraftModel;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MaintenanceTemplate {
    @Id
    @Column(name = "maintenance_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    @Column ( nullable = false )
    private String name;
    @Enumerated(EnumType.STRING)
    @Column ( nullable = false )
    private MaintenanceType templateType;
    @ManyToMany
    @CollectionTable(name = "maintenance_models", joinColumns = @JoinColumn(name = "maintenance_id"))
    private List<AircraftModel> applicableModels = new ArrayList<>();
    @ElementCollection
    @Column ( nullable = false )
    private List<String> checklist;
    @Column ( nullable = false )
    private Integer intervalFlightHours;
    @Column ( nullable = false )
    private Integer intervalDays;

    public MaintenanceTemplate() {}
    public MaintenanceTemplate(String name, MaintenanceType templateType, List<AircraftModel> applicableModels, List<String> checklist, Integer intervalFlightHours, Integer intervalDays) {
        Assert.notNull(name,"Name cannot be null");
        Assert.notNull(templateType, "Template cannot be null");
        Assert.notNull(applicableModels,"Template must have applicable models");
        Assert.notNull(checklist,"Template must have a checklist");
        Assert.notNull(intervalFlightHours,"Template must have an interval in flight hours");
        Assert.notNull(intervalDays,"Template must have an interval in days");
        Assert.hasText(name,"Name cannot be empty");
        this.name = name;
        this.templateType = templateType;
        this.applicableModels = applicableModels;
        this.checklist = checklist;
        this.intervalFlightHours = intervalFlightHours;
        this.intervalDays = intervalDays;
    }

}
