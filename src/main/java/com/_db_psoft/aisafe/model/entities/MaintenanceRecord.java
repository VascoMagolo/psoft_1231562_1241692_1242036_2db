package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.MaintenanceComponent;
import com._db_psoft.aisafe.model.enums.MaintenanceStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;


@Entity

public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id1;
    private Integer id;
    private String description;
    private Date startDate;
    private Integer expectedDuration;
    private String notes;
    private MaintenanceComponent component;
    private MaintenanceStatus status;
}
