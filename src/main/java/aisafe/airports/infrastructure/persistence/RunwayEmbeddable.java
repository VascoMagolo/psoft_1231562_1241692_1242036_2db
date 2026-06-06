package aisafe.airports.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class RunwayEmbeddable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer length;

    @Column(nullable = false)
    private String orientation;

    public RunwayEmbeddable() {}

    public RunwayEmbeddable(String name, Integer length, String orientation) {
        this.name = name;
        this.length = length;
        this.orientation = orientation;
    }
}
