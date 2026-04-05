package com._db_psoft.aisafe.model.classes;

import com._db_psoft.aisafe.model.enums.Role;

public class User {
    private Integer id;
    private String username;
    private String passwordHash;
    private Role role;
}
