package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.RouteStatus;

public class Route {
    private Integer id;
    public Integer estimatedFlightTime;
    public Double minRangeRequirement;
    public Integer minCapacityRequirement;
    protected RouteStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEstimatedFlightTime() {
        return estimatedFlightTime;
    }

    public void setEstimatedFlightTime(Integer estimatedFlightTime) {
        this.estimatedFlightTime = estimatedFlightTime;
    }

    public Double getMinRangeRequirement() {
        return minRangeRequirement;
    }

    public void setMinRangeRequirement(Double minRangeRequirement) {
        this.minRangeRequirement = minRangeRequirement;
    }

    public Integer getMinCapacityRequirement() {
        return minCapacityRequirement;
    }

    public void setMinCapacityRequirement(Integer minCapacityRequirement) {
        this.minCapacityRequirement = minCapacityRequirement;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }
}
