package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;
import aisafe.shared.domain.DomainException;

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

        if (originCode == null || originCode.isBlank()) {
            throw new DomainException("Origin cannot be blank");
        }
        if (destinationCode == null || destinationCode.isBlank()) {
            throw new DomainException("Destination cannot be blank");
        }
        if (estimatedFlightTime == null) {
            throw new DomainException("estimatedFlightTime must not be null");
        }
        if (estimatedFlightTime <= 0) {
            throw new DomainException("Invalid flight time");
        }
        if (minimumRange == null) {
            throw new DomainException("minimumRange must not be null");
        }
        if (minimumRange <= 0) {
            throw new DomainException("Invalid minimum range");
        }
        if (minimumCapacity == null) {
            throw new DomainException("minimumCapacity must not be null");
        }
        if (minimumCapacity <= 0) {
            throw new DomainException("Invalid minimum capacity");
        }
        if (originCode.trim().equalsIgnoreCase(destinationCode.trim())) {
            throw new DomainException("Origin and destination cannot be the same");
        }

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
            if (flightTime <= 0) {
                throw new DomainException("Invalid flight time");
            }
            this.estimatedFlightTime = flightTime;
        }
        if (range != null) {
            if (range <= 0) {
                throw new DomainException("Invalid minimum range");
            }
            this.minimumRange = range;
        }
        if (capacity != null) {
            if (capacity <= 0) {
                throw new DomainException("Invalid minimum capacity");
            }
            this.minimumCapacity = capacity;
        }
    }

}
