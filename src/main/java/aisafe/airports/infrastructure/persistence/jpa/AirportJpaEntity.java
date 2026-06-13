package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "airport")
@Getter
@Setter
public class AirportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(length = 3, nullable = false, unique = true)
    private String iataCode;

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

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "airport_runways", joinColumns = @JoinColumn(name = "airport_id"))
    private List<RunwayEmbeddable> runways = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_contacts", joinColumns = @JoinColumn(name = "airport_id"))
    private List<ContactEmbeddable> contacts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_services", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "description")
    private List<String> services = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_terminals", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "name")
    private List<String> terminals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_gates", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "identifier")
    private List<String> gates = new ArrayList<>();

    public AirportJpaEntity() {}
}
