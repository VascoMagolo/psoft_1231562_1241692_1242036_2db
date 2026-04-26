package aisafe.security.application.dtos;

import aisafe.security.domain.Role;

public record RegisterRequest(String username, String password, Role role) {
}
