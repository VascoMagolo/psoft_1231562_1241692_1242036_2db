package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.MaintenanceComponent;
import com._db_psoft.aisafe.model.enums.MaintenanceStatus;

import java.util.Date;

public class MaintenanceRecord {
    private Integer id;
    private String description;
    private Date startDate;
    private Integer expectedDuration;
    private String notes;
    private MaintenanceComponent component;
    private MaintenanceStatus status;
}
