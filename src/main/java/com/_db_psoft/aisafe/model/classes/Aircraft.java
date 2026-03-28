package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.AircraftFeatures;
import com._db_psoft.aisafe.model.enums.AircraftStatus;

import java.util.Date;
import java.util.List;

public class Aircraft {
    public String registrationNumber;
    public Date manufacturingDate;
    public AircraftStatus status;
    public AircraftModel model;
    public List<AircraftFeatures> features;

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public Date getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(Date manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public AircraftStatus getStatus() {
        return status;
    }

    public void setStatus(AircraftStatus status) {
        this.status = status;
    }

    public AircraftModel getModel() {
        return model;
    }

    public void setModel(AircraftModel model) {
        this.model = model;
    }

    public List<AircraftFeatures> getFeatures() {
        return features;
    }

    public void setFeatures(List<AircraftFeatures> features) {
        this.features = features;
    }
}
