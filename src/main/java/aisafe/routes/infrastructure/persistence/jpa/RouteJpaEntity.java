package aisafe.routes.infrastructure.persistence.jpa;

import aisafe.routes.domain.RouteStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "route")
@Getter
@Setter
public class RouteJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_iata_code", length = 3, nullable = false)
    private String originCode;

    @Column(name = "destination_iata_code", length = 3, nullable = false)
    private String destinationCode;

    @Column(nullable = false)
    private Integer estimatedFlightTime;

    @Column(nullable = false)
    private Double minimumRange;

    @Column(nullable = false)
    private Integer minimumCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status;

    @Version
    private Long version;

    protected RouteJpaEntity() {}

    public RouteJpaEntity(String originCode, String destinationCode, Integer estimatedFlightTime,
                          Double minimumRange, Integer minimumCapacity, RouteStatus status) {
        this.originCode = originCode;
        this.destinationCode = destinationCode;
        this.estimatedFlightTime = estimatedFlightTime;
        this.minimumRange = minimumRange;
        this.minimumCapacity = minimumCapacity;
        this.status = status;
    }
}
