package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.MaintenanceType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class MaintenanceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private MaintenanceType templateType;
    private List<AircraftModel> applicableModels;
    private List<String> checklist;
}
