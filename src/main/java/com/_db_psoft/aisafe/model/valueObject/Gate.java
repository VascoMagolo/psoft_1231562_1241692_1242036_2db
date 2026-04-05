package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Gate {
    private String identifier;

    protected Gate() {}

    public Gate(String identifier) {
        this.identifier = identifier;
    }
}
