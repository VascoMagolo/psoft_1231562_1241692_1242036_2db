package com._db_psoft.aisafe.model.valueObject;

import com._db_psoft.aisafe.model.enums.ContactType;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Contact {
    private ContactType type;
    private String value;
    private String description;

    protected Contact() {}

    public Contact(ContactType type, String value, String description){
        this.type = type;
        this.description = description;
        this.value = value;
    }
}
