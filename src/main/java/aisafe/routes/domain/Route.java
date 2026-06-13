package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class Route {

    private IataCode origin;
    private IataCode destination;
    private Integer estimatedFlightTime;
    private Double minimumRange;
    private Integer minimumCapacity;
    private RouteStatus status;
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
        this.status = RouteStatus.ACTIVE;
    }

    public void setVersion(Long version) { this.version = version; }
    public void setStatus(RouteStatus status) { this.status = status; }

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

}
