package aisafe.routes.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_history")
@Getter
@Setter
public class RouteHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteJpaEntity route;

    @Column(nullable = false)
    private String changeDescription;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String changedBy;

    protected RouteHistoryJpaEntity() {}

    public RouteHistoryJpaEntity(RouteJpaEntity route, String changeDescription,
                                  LocalDateTime changedAt, String changedBy) {
        this.route = route;
        this.changeDescription = changeDescription;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }
}
