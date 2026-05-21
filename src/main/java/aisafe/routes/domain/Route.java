package aisafe.routes.domain;

import aisafe.model.valueObject.IataCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "origin_iata_code"))
    })
    private IataCode origin;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "destination_iata_code"))
    })
    private IataCode destination;

    @Column(nullable = false)
    private Integer estimatedFlightTime;

    @Column(nullable = false)
    private Double minimumRange;

    @Column(nullable = false)
    private Integer minimumCapacity;

    @Setter
    @Column(nullable = false)
    private boolean active;

    @Version
    private Long version;

    protected Route() {}

    public Route(String originCode, String destinationCode, Integer estimatedFlightTime,
                 Double minimumRange, Integer minimumCapacity) {

        if (originCode == null || originCode.isBlank()) throw new IllegalArgumentException("Origin cannot be blank");
        if (destinationCode == null || destinationCode.isBlank()) throw new IllegalArgumentException("Destination cannot be blank");
        if (estimatedFlightTime == null || estimatedFlightTime <= 0) throw new IllegalArgumentException("Invalid flight time");

        this.origin = new IataCode(originCode);
        this.destination = new IataCode(destinationCode);
        this.estimatedFlightTime = estimatedFlightTime;
        this.minimumRange = minimumRange;
        this.minimumCapacity = minimumCapacity;
        this.active = true;
    }

    public void updateRoute(Integer flightTime, Double range, Integer capacity) {
        if (flightTime != null && flightTime > 0) this.estimatedFlightTime = flightTime;
        if (range != null && range > 0) this.minimumRange = range;
        if (capacity != null && capacity > 0) this.minimumCapacity = capacity;
    }
}