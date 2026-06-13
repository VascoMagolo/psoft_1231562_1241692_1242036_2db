package aisafe.routes.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
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

    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
