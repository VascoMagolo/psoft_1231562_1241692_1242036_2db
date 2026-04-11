package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.MaintenanceType;
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
