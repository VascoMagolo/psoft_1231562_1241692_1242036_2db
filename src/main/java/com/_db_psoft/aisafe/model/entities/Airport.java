package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.AirportStatus;
import com._db_psoft.aisafe.model.valueObject.*;
import jakarta.persistence.*;

@Entity

public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String city;
    private String country;
    private String region;
    private String timezone;
    private String imagePath;
    private String operationalHours;
    private AirportStatus operationalStatus;

    @Embedded
    private Coordinates coordinates;
    @Embedded
    private IataCode iataCode;
    @Embedded
    private Runway runway;
    @Embedded
    private Contact contact;
    @Embedded
    private Terminal terminal;
    @Embedded
    private Gate gate;
    @Embedded
    private Service service;
}
