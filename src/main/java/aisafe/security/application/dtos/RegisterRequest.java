package aisafe.security.application.dtos;

import aisafe.security.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for user registration requests.
 * @param username the desired username for the new user
 * @param password the desired password for the new user
 * @param role the role to assign to the new user
 */
public record RegisterRequest(@NotBlank String username, @NotBlank String password, @NotNull Role role) {
}
