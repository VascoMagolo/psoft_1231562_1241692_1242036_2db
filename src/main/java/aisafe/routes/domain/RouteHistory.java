package aisafe.routes.domain;

import aisafe.airports.domain.IataCode;

import java.time.LocalDateTime;

public class RouteHistory {

    private IataCode originCode;
    private IataCode destinationCode;
    private String changeDescription;
    private LocalDateTime changedAt;
    private String changedBy;

    protected RouteHistory() {}

    public RouteHistory(IataCode originCode, IataCode destinationCode, String changeDescription, String changedBy) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.changeDescription = changeDescription;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    public RouteHistory(IataCode originCode, IataCode destinationCode, String changeDescription,
                        LocalDateTime changedAt, String changedBy) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.changeDescription = changeDescription;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    public IataCode getOriginCode() { return originCode; }
    public IataCode getDestinationCode() { return destinationCode; }
    public String getChangeDescription() { return changeDescription; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }
}
