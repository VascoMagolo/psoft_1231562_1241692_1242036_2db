package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Runway {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Integer length;
    @Column(nullable = false)
    private String orientation;

    protected Runway() {}

    public Runway(String name, Integer length, String orientation) {
        if (name.trim().isEmpty() || orientation.trim().isEmpty() || length == null){
            throw new IllegalArgumentException("Name, length or orientation cannot be null");
        }
        this.length = length;
        this.name = name;
        this.orientation = orientation;
    }
}
