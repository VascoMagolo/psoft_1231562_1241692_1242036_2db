package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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

    @Embedded
    private IataCodeJpaEmbeddable iataCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String region;

    @Column(nullable = false)
    private String timezone;

    @ElementCollection
    @CollectionTable(name = "airport_photos", joinColumns = @JoinColumn(name = "airport_id"))
    @OrderColumn(name = "photo_index")
    @BatchSize(size = 25)
    private List<AirportPhotoJpaEmbeddable> photos = new ArrayList<>();

    private String operationalHours;

    @Column(nullable = false)
    private String status;

    @Embedded
    private CoordinatesJpaEmbeddable coordinates;

    @ElementCollection
    @CollectionTable(name = "airport_runways", joinColumns = @JoinColumn(name = "airport_id"))
    @BatchSize(size = 25)
    private List<RunwayJpaEmbeddable> runways = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_contacts", joinColumns = @JoinColumn(name = "airport_id"))
    @BatchSize(size = 25)
    private List<ContactJpaEmbeddable> contacts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_services", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "description")
    @BatchSize(size = 25)
    private List<String> services = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_terminals", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "name")
    @BatchSize(size = 25)
    private List<String> terminals = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "airport_gates", joinColumns = @JoinColumn(name = "airport_id"))
    @Column(name = "identifier")
    @BatchSize(size = 25)
    private List<String> gates = new ArrayList<>();

    public AirportJpaEntity() {}
}
