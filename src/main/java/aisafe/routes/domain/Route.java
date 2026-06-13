package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import org.springframework.util.Assert;

public class Route {

    private IataCode origin;
    private IataCode destination;
    private Integer estimatedFlightTime;
    private Double minimumRange;
    private Integer minimumCapacity;
    private RouteStatus status;

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

    public IataCode getOrigin() { return origin; }
    public IataCode getDestination() { return destination; }
    public Integer getEstimatedFlightTime() { return estimatedFlightTime; }
    public Double getMinimumRange() { return minimumRange; }
    public Integer getMinimumCapacity() { return minimumCapacity; }
    public RouteStatus getStatus() { return status; }

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
