package aisafe.security.application.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a login request, containing the username and password.
 * @param username the username of the user attempting to log in
 * @param password the password of the user attempting to log in
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
