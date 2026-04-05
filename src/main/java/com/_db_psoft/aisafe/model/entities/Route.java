package com._db_psoft.aisafe.model.entities;

import com._db_psoft.aisafe.model.enums.RouteStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id1;
    private Integer id;
    private Double distance;
    private Integer estimatedFlightTime;
    private Double minRangeRequirement;
    private Integer minCapacityRequirement;
    private RouteStatus status;

}
