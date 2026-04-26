package aisafe.security.domain;

import aisafe.DomainException;

/**
 * Thrown when a login attempt fails due to an unrecognized username or incorrect password.
 *
 * <p>The message is intentionally generic — callers must not reveal which of the two
 * fields was wrong, to avoid username enumeration attacks.</p>
 */

public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
