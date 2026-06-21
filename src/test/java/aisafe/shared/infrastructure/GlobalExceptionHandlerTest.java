package aisafe.shared.infrastructure;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.security.domain.InvalidCredentialsException;
import aisafe.shared.domain.ConcurrencyException;
import aisafe.shared.domain.DomainException;
import aisafe.shared.domain.DuplicateResourceException;
import aisafe.shared.domain.ResourceInUseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void ensureHandleInvalidCredentials() {
        var resp = handler.handleInvalidCredentials(new InvalidCredentialsException());
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Invalid credentials", resp.getBody().message());
    }

    @Test
    void ensureHandleNotFound() {
        var resp = handler.handleNotFound(new AircraftNotFoundException("not found"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("not found", resp.getBody().message());
    }

    @Test
    void ensureHandleBadRequest() {
        var resp = handler.handleBadRequest(new DomainException("bad request"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("bad request", resp.getBody().message());
    }

    @Test
    void ensureHandleConflict() {
        var resp1 = handler.handleConflict(new DuplicateResourceException("duplicate"));
        assertEquals(HttpStatus.CONFLICT, resp1.getStatusCode());
        assertEquals("duplicate", resp1.getBody().message());

        var resp2 = handler.handleConflict(new ResourceInUseException("in use"));
        assertEquals(HttpStatus.CONFLICT, resp2.getStatusCode());
        assertEquals("in use", resp2.getBody().message());

        var resp3 = handler.handleConflict(new DataIntegrityViolationException("unique constraint"));
        assertEquals(HttpStatus.CONFLICT, resp3.getStatusCode());
        assertEquals("A resource with the given unique identifier already exists.", resp3.getBody().message());
    }

    @Test
    void ensureHandleForbidden() {
        var resp = handler.handleForbidden(new AccessDeniedException("denied"));
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertEquals("You do not have permission to access this resource.", resp.getBody().message());
    }

    @Test
    void ensureHandleOptimisticLock() {
        var resp = handler.handleOptimisticLock(Mockito.mock(ObjectOptimisticLockingFailureException.class));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals("The resource was modified by another request. Please retry.", resp.getBody().message());
    }

    @Test
    void ensureHandlePreconditionFailed() {
        var resp = handler.handlePreconditionFailed(new ConcurrencyException("precondition failed"));
        assertEquals(HttpStatus.PRECONDITION_FAILED, resp.getStatusCode());
        assertEquals("precondition failed", resp.getBody().message());
    }

    @Test
    void ensureHandleTypeMismatch() {
        var ex = Mockito.mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("invalid-value");
        when(ex.getName()).thenReturn("param-name");

        var resp = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Invalid value 'invalid-value' for parameter 'param-name'.", resp.getBody().message());
    }

    @Test
    void ensureHandleValidationErrors() {
        var bindingResult = Mockito.mock(BindingResult.class);
        var fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var resp = handler.handleValidationErrors(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Validation failed", resp.getBody().message());
        assertEquals(Map.of("fieldName", "defaultMessage"), resp.getBody().errors());
    }

    @Test
    void ensureHandleAllOtherExceptions() {
        var resp = handler.handleAllOtherExceptions(new Exception("fatal error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("An unexpected internal server error occurred: fatal error", resp.getBody().message());
    }
}
