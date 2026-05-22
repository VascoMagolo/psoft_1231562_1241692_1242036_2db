package aisafe.routes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

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

    public RouteHistory(Route route, String changeDescription, String changedBy) {
        this.route = route;
        this.changeDescription = changeDescription;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }
}