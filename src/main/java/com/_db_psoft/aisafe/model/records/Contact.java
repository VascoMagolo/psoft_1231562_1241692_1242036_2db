package com._db_psoft.aisafe.model.records;

import com._db_psoft.aisafe.model.enums.ContactType;

public record Contact(ContactType type, String value, String description) {
}
