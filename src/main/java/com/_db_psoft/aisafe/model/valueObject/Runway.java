package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Runway {
    private String name;
    private Integer length;
    private String orientation;

    protected Runway() {}

    public Runway(String name, Integer length, String orientation) {
        this.length = length;
        this.name = name;
        this.orientation = orientation;
    }
}
