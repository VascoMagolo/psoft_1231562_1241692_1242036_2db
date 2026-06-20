package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CoordinatesJpaEmbeddable {
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public CoordinatesJpaEmbeddable() {}

    public CoordinatesJpaEmbeddable(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
