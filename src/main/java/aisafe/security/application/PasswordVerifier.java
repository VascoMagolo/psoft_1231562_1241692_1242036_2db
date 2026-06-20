package aisafe.security.application;

public interface PasswordVerifier {
    boolean matches(String raw, String encoded);
}
