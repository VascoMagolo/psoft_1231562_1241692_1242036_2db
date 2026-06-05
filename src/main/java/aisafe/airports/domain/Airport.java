package aisafe.airports.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an airport in the system.
 */
public class Airport {

    private final IataCode iataCode;
    private final String name;
    private final String city;
    private final String country;
    private final String region;
    private final String timezone;
    private final Coordinates coordinates;
    private final List<Runway> runways;
    private AirportStatus status;
    private String imagePath;
    private String operationalHours;
    private List<Contact> contacts;
    private List<Service> services;
    private List<Terminal> terminals;
    private List<Gate> gates;

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

    public void setStatus(AirportStatus status) { this.status = status; }

    public IataCode getIataCode() { return iataCode; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getRegion() { return region; }
    public String getTimezone() { return timezone; }
    public Coordinates getCoordinates() { return coordinates; }
    public List<Runway> getRunways() { return runways; }
    public AirportStatus getStatus() { return status; }
    public String getImagePath() { return imagePath; }
    public String getOperationalHours() { return operationalHours; }
    public List<Contact> getContacts() { return contacts; }
    public List<Service> getServices() { return services; }
    public List<Terminal> getTerminals() { return terminals; }
    public List<Gate> getGates() { return gates; }
}
