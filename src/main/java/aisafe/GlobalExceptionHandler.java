package aisafe;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.exceptions.InvalidContactException;
import aisafe.exceptions.InvalidIataCodeException;
import aisafe.security.domain.InvalidCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

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

    /** 404 Not Found */
    @ExceptionHandler({AircraftNotFoundException.class, AirportNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
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

    /** 409 Conflict - optimistic locking collision (concurrent update detected) */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("The resource was modified by another request. Please retry."));
    }

    /** 400 Bad Request - bean validation failure (@Valid on request bodies) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("Validation failed", errors));
    }

    /** 500 Internal Server Error - Catch all for unexpected error */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected internal server error occurred: " + ex.getMessage()));
    }

    /** Error response body. */
    record ErrorResponse(String message) {}

    /** Error response body for validation failures — includes per-field details. */
    record ValidationErrorResponse(String message, Map<String, String> errors) {}

}
