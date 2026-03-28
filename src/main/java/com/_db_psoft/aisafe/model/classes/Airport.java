package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.AirportStatus;

import java.util.List;

public class Airport {
    public String name;
    public String city;
    public String country;
    public String timezone;
    public String imagePath;
    public String operationalHours;
    public String contactInformation;
    public List<String> facilityInformation;
    protected AirportStatus status;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public AirportStatus getStatus() {
        return status;
    }

    public void setStatus(AirportStatus status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOperationalHours() {
        return operationalHours;
    }

    public void setOperationalHours(String operationalHours) {
        this.operationalHours = operationalHours;
    }

    public String getContactInformation() {
        return contactInformation;
    }

    public void setContactInformation(String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public List<String> getFacilityInformation() {
        return facilityInformation;
    }

    public void setFacilityInformation(List<String> facilityInformation) {
        this.facilityInformation = facilityInformation;
    }
}
