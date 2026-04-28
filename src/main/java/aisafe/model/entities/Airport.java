package aisafe.model.entities;

import aisafe.aircrafts.domain.AirportStatus;
import aisafe.model.valueObject.*;
import jakarta.persistence.*;

import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private AirportStatus operationalStatus;

    @Embedded
    private Coordinates coordinates;
    @Embedded
    private IataCode iataCode;
    @ElementCollection
    private List<Contact> contacts;
    @ElementCollection
    private List<Service> services;
    @ElementCollection
    private List<Terminal> terminals;
    @ElementCollection
    private List<Gate> gates;
    @ElementCollection
    private List<Runway> runways;
}
