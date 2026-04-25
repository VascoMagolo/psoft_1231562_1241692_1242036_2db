package aisafe.model.entities;

import aisafe.model.enums.RouteStatus;
import jakarta.persistence.*;

@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double distance;
    private Integer estimatedFlightTime;
    private Double minRangeRequirement;
    private Integer minCapacityRequirement;
    @Enumerated(EnumType.STRING)
    private RouteStatus status;

}
