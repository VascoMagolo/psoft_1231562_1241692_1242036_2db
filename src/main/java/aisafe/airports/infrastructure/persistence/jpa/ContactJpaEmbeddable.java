package aisafe.airports.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ContactJpaEmbeddable {

    @Column(nullable = false)
    private String type;

    @Column(name = "contact_value", nullable = false)
    private String value;

    private String description;

    public ContactJpaEmbeddable() {}

    public ContactJpaEmbeddable(String type, String value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }
}
