package aisafe.routes.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RouteHistory {

    private Long id;
    private Long routeId;
    private String changeDescription;
    private LocalDateTime changedAt;
    private String changedBy;

    protected RouteHistory() {}

    public RouteHistory(Long routeId, String changeDescription, String changedBy) {
        this.routeId = routeId;
        this.changeDescription = changeDescription;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    public void setId(Long id) { this.id = id; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
