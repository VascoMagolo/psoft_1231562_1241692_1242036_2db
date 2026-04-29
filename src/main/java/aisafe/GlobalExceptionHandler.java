package aisafe;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.exceptions.InvalidContactException;
import aisafe.exceptions.InvalidIataCodeException;
import aisafe.security.domain.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    /** 401 Unauthorized — authentication failed. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 404 Not Found - ex. aircraft not found */
    // can be added more later
    @ExceptionHandler(AircraftNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound (RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 401 Bad Request - Invalid args.*/
    // can be added more later
    @ExceptionHandler({
            DomainException.class,
            IllegalArgumentException.class,
            InvalidIataCodeException.class,
            InvalidContactException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /** 409 Conflict - duplicated information */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict (DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("A resource with the given unique identifier already exists."));
    }

    /** 403 Forbidden - Token is valid, but user doesnt have needed role */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("You do not have permission to access this resource."));
    }

    /** 500 Internal Server Error - Catch all for unexpected error */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected internal server error occurred: " + ex.getMessage()));
    }

    /** Error response body. */
    record ErrorResponse(String message) {}

}
