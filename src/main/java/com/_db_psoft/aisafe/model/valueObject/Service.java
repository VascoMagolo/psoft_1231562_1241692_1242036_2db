package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Service {
    @Column(nullable = false)
    private String description;

    protected Service() {}

    public Service(String description) {
        if (description.trim().isEmpty()) throw new IllegalArgumentException("Service description cannot be empty");
        this.description = description;
    }
}
