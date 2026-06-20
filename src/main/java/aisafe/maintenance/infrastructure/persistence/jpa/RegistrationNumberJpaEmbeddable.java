package aisafe.maintenance.infrastructure.persistence.jpa;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class RegistrationNumberJpaEmbeddable {
    private String number;

    public RegistrationNumberJpaEmbeddable() {}

    public RegistrationNumberJpaEmbeddable(String number) {
        this.number = number;
    }
}
