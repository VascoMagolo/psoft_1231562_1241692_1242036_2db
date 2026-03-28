package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.MaintenanceType;

import java.util.List;

public class MaintenanceTemplate {
    public String name;
    public MaintenanceType templateType;
    public List<AircraftModel> applicableModels;
    public List<String> checklist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MaintenanceType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(MaintenanceType templateType) {
        this.templateType = templateType;
    }

    public List<String> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<String> checklist) {
        this.checklist = checklist;
    }

    public List<AircraftModel> getApplicableModels() {
        return applicableModels;
    }

    public void setApplicableModels(List<AircraftModel> applicableModels) {
        this.applicableModels = applicableModels;
    }
}
