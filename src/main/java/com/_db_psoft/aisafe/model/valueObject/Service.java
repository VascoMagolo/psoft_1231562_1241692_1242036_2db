package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Service {
    private String description;

    protected Service() {}

    public Service(String description) {
        this.description = description;
    }
}
