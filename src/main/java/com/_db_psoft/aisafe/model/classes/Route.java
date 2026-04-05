package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.RouteStatus;

public class Route {
    private Integer id;
    private Double distance;
    private Integer estimatedFlightTime;
    private Double minRangeRequirement;
    private Integer minCapacityRequirement;
    private RouteStatus status;

}
