package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.MaintenanceType;

import java.util.List;

public class MaintenanceTemplate {
    private String name;
    private MaintenanceType templateType;
    private List<AircraftModel> applicableModels;
    private List<String> checklist;
}
