package aisafe.security.application.dtos;

/**
 * DTO representing the response of an authentication request, containing the generated token.
 * @param token the authentication token generated for the user upon successful authentication.
 */
public record AuthResponse(String token) {
}
