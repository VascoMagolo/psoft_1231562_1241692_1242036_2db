package aisafe.routes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Entity responsible for storing the history of changes
 * made to a flight route.
 */
@Entity
@Getter
public class RouteHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false)
    private String changeDescription;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String changedBy;

    protected RouteHistory() {}

    /**
     * Creates a new route history entry.
     *
     * @param route associated route
     * @param changeDescription description of the performed change
     * @param changedBy user responsible for the change
     */
    public RouteHistory(Route route, String changeDescription, String changedBy) {
        this.route = route;
        this.changeDescription = changeDescription;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }
}