package aisafe.aircrafts.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Data model for aircraft registration numbers in the persistence layer.
 */
@Embeddable
@Getter
@Setter
public class RegistrationNumberJpaEmbeddable {

    @Column(name = "registration_number", unique = true, nullable = false)
    private String number;

    public RegistrationNumberJpaEmbeddable() {}

    public RegistrationNumberJpaEmbeddable(String number) {
        this.number = number;
    }
}
