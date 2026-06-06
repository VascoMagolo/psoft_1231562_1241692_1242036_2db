package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.util.Assert;

/**
 * Entity representing a flight route between two airports.
 * Stores operational requirements such as flight time,
 * minimum range and passenger capacity.
 */

@Entity
@Getter
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "origin_iata_code", length = 3, nullable = false))
    })
    private IataCode origin;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "code", column = @Column(name = "destination_iata_code", length = 3, nullable = false))
    })
    private IataCode destination;

    @Column(nullable = false)
    private Integer estimatedFlightTime;

    @Column(nullable = false)
    private Double minimumRange;

    @Column(nullable = false)
    private Integer minimumCapacity;

    @Column(nullable = false)
    private boolean active;

    @Version
    private Long version;

    protected Route() {}

    public Route(String originCode, String destinationCode, Integer estimatedFlightTime,
                 Double minimumRange, Integer minimumCapacity) {

        Assert.hasText(originCode, "Origin cannot be blank");
        Assert.hasText(destinationCode, "Destination cannot be blank");
        Assert.notNull(estimatedFlightTime, "estimatedFlightTime must not be null");
        Assert.isTrue(estimatedFlightTime > 0, "Invalid flight time");
        Assert.notNull(minimumRange, "minimumRange must not be null");
        Assert.isTrue(minimumRange > 0, "Invalid minimum range");
        Assert.notNull(minimumCapacity, "minimumCapacity must not be null");
        Assert.isTrue(minimumCapacity > 0, "Invalid minimum capacity");
        Assert.isTrue(!originCode.trim().equalsIgnoreCase(destinationCode.trim()), "Origin and destination cannot be the same");

        this.origin = new IataCode(originCode);
        this.destination = new IataCode(destinationCode);

        this.estimatedFlightTime = estimatedFlightTime;
        this.minimumRange = minimumRange;
        this.minimumCapacity = minimumCapacity;
        this.active = true;
    }

    public void updateRoute(Integer flightTime, Double range, Integer capacity) {
        if (flightTime != null) {
            Assert.isTrue(flightTime > 0, "Invalid flight time");
            this.estimatedFlightTime = flightTime;
        }
        if (range != null) {
            Assert.isTrue(range > 0, "Invalid minimum range");
            this.minimumRange = range;
        }
        if (capacity != null) {
            Assert.isTrue(capacity > 0, "Invalid minimum capacity");
            this.minimumCapacity = capacity;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
