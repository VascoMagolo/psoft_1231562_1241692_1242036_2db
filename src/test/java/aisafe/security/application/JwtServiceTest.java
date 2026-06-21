package aisafe.security.application;

import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void ensureGenerateAndValidateToken() {
        User user = new User("testuser", "password", Role.ADMIN);
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("testuser", jwtService.extractSubject(token));
        assertEquals(List.of("ADMIN"), jwtService.extractRoles(token));
    }

    @Test
    void ensureIsTokenValidReturnsFalseForInvalidOrMalformedToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
        assertFalse(jwtService.isTokenValid(""));
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void ensureIsTokenValidReturnsFalseForExpiredToken() {
        // Create a JwtService with negative/zero expiration
        JwtService expiredService = new JwtService(secret, -1000);
        User user = new User("expireduser", "password", Role.BACKOFFICE_OPERATOR);
        String token = expiredService.generateToken(user);
        assertNotNull(token);
        assertFalse(jwtService.isTokenValid(token));
    }
}
