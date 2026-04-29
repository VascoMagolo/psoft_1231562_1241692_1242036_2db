package aisafe.model.entities;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.model.enums.MaintenanceType;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class MaintenanceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private MaintenanceType templateType;
    @ManyToMany
    private List<AircraftModel> applicableModels;
    @ElementCollection
    private List<String> checklist;
    private Integer intervalFlightHours;
    private Integer intervalDays;
}
