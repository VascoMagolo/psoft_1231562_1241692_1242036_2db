package aisafe.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Gate {
    @Column(nullable = false)
    private String identifier;

    protected Gate() {}

    public Gate(String identifier) {
        if(identifier == null||identifier.trim().isEmpty()) throw new IllegalArgumentException("Gate identifier cannot be empty");
        this.identifier = identifier;
    }
}
