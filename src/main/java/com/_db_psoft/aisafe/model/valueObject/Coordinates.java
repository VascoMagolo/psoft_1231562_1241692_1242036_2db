package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Coordinates {
    private Double latitude;
    private Double longitude;

    protected Coordinates() {}

    public Coordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
