package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class RunwayJpaEmbeddable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer length;

    @Column(nullable = false)
    private String orientation;

    public RunwayJpaEmbeddable() {}

    public RunwayJpaEmbeddable(String name, Integer length, String orientation) {
        this.name = name;
        this.length = length;
        this.orientation = orientation;
    }
}
