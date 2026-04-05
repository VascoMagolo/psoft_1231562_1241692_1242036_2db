package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Terminal {
    private String name;

    protected Terminal() {}

    public Terminal(String name){
        this.name = name;
    }
}
