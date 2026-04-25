package aisafe;

import aisafe.security.domain.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler that translates domain and infrastructure exceptions into
 * meaningful HTTP responses.
 *
 * <p>Annotated with {@link RestControllerAdvice}, which applies to every
 * {@code @RestController} in the application.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /** 401 — authentication failed. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Add more based on necessities

    /** Error response body. */
    record ErrorResponse(String message) {}

}
