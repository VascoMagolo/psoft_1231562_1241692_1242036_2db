package aisafe.model.entities;

import aisafe.airports.domain.Airport;
import aisafe.model.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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

    @ManyToOne
    @JoinColumn(name = "origin_airport_id")
    private Airport originAirport;

    @ManyToOne
    @JoinColumn(name = "destination_airport_id")
    private Airport destinationAirport;
}
