package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class IataCodeJpaEmbeddable {
    @Column(name = "iata_code", length = 3, nullable = false, unique = true)
    private String code;

    public IataCodeJpaEmbeddable() {}

    public IataCodeJpaEmbeddable(String code) {
        this.code = code;
    }
}
