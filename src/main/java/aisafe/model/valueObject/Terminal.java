package aisafe.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Terminal {
    @Column(nullable = false)
    private String name;

    protected Terminal() {}

    public Terminal(String name){
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Terminal name cannot be empty");
        this.name = name;
    }
}
