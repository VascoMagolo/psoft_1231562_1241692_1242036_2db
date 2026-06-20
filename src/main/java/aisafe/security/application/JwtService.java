package aisafe.security.application;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Service for generating and validating JSON Web Tokens (JWT) for user authentication and authorization.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;


    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT for the given user, including their role as a claim.
     * @param user the user for whom to generate the token
     * @return a JWT string that can be used for authentication and authorization
     */
    public String generateToken(User user) {
        List<String> roles = List.of(user.getRole().name());

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the subject (username) from the given JWT token.
     * @param token the JWT token from which to extract the subject
     * @return the subject (username) contained in the token, or null if the token is invalid
     */
    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the roles from the given JWT token.
     * @param token the JWT token from which to extract the roles
     * @return a list of roles contained in the token, or an empty list if the token is invalid or does not contain roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    /**
     * Validates the given JWT token by checking its signature and expiration.
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses the claims from the given JWT token.
     * @param token the JWT token to parse
     * @return the claims contained in the token, or null if the token is invalid
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}