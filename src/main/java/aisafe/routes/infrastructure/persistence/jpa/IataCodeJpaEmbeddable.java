package aisafe.routes.infrastructure.persistence.jpa;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class IataCodeJpaEmbeddable {
    private String code;

    public IataCodeJpaEmbeddable() {}

    public IataCodeJpaEmbeddable(String code) {
        this.code = code;
    }
}
