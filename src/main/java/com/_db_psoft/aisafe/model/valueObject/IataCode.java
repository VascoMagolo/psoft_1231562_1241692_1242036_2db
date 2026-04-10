package com._db_psoft.aisafe.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class IataCode {
    @Column(length = 3, nullable = false, unique = true)
    private String code;

    protected IataCode() {}

    public IataCode(String code) {
        this.code = code;
    }
}
