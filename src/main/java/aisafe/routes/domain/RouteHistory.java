package aisafe.routes.domain;

import java.time.LocalDateTime;

public class RouteHistory {

    private String originCode;
    private String destinationCode;
    private String changeDescription;
    private LocalDateTime changedAt;
    private String changedBy;

    protected RouteHistory() {}

    public RouteHistory(String originCode, String destinationCode, String changeDescription, String changedBy) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.changeDescription = changeDescription;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    public String getOriginCode() { return originCode; }
    public String getDestinationCode() { return destinationCode; }
    public String getChangeDescription() { return changeDescription; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getChangedBy() { return changedBy; }

    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
