package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.AircraftStatus;

import java.time.LocalDate;
import java.util.List;

public class Aircraft {
    private String registrationNumber;
    private LocalDate manufacturingDate;
    private AircraftStatus status;
    private Integer seatCapacity;
    private AircraftModel model;
    private List<String> features;
}
