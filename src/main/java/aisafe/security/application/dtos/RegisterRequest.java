package aisafe.security.application.dtos;

import aisafe.security.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(@NotBlank String username, @NotBlank String password, @NotNull Role role) {
}
