package aisafe.airports.domain;

import aisafe.model.valueObject.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private IataCode iataCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String region;

    @Column(nullable = false)
    private String timezone;

    private String imagePath;
    private String operationalHours;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportStatus status;

    @Embedded
    private Coordinates coordinates;

    @ElementCollection
    @CollectionTable(name = "airport_runways", joinColumns = @JoinColumn(name = "airport_id"))
    private List<Runway> runways = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_contacts", joinColumns = @JoinColumn(name = "airport_id"))
    private List<Contact> contacts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_services", joinColumns = @JoinColumn(name = "airport_id"))
    private List<Service> services = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_terminals", joinColumns = @JoinColumn(name = "airport_id"))
    private List<Terminal> terminals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_gates", joinColumns = @JoinColumn(name = "airport_id"))
    private List<Gate> gates = new ArrayList<>();

    @Version
    private Long version;

    protected Airport() {}

    public Airport(String iataCode, String name, String city, String country, String region,
                   String timezone, Double latitude, Double longitude, List<Runway> runways) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Airport name cannot be blank");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City cannot be blank");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country cannot be blank");
        if (timezone == null || timezone.isBlank()) throw new IllegalArgumentException("Timezone cannot be blank");
        if (runways == null || runways.isEmpty()) throw new IllegalArgumentException("Airport must have at least one runway");

        this.iataCode = new IataCode(iataCode);
        this.name = name.trim();
        this.city = city.trim();
        this.country = country.trim();
        this.region = (region != null) ? region.trim() : null;
        this.timezone = timezone.trim();
        this.coordinates = new Coordinates(latitude, longitude);
        this.runways = new ArrayList<>(runways);
        this.status = AirportStatus.OPERATIONAL;
        this.contacts = new ArrayList<>();
        this.services = new ArrayList<>();
        this.terminals = new ArrayList<>();
        this.gates = new ArrayList<>();
    }

    public void updateDetails(String operationalHours, List<Contact> contacts, String imagePath,
                              List<Service> services, List<Terminal> terminals, List<Gate> gates) {
        if (operationalHours != null) this.operationalHours = operationalHours;
        if (contacts != null) this.contacts = new ArrayList<>(contacts);
        if (imagePath != null) this.imagePath = imagePath;
        if (services != null) this.services = new ArrayList<>(services);
        if (terminals != null) this.terminals = new ArrayList<>(terminals);
        if (gates != null) this.gates = new ArrayList<>(gates);
    }
}
