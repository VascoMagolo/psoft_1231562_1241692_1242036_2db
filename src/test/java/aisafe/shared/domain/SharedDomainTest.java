package aisafe.shared.domain;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SharedDomainTest {

    @Test
    void ensureDomainExceptionProperties() {
        DomainException ex = new DomainException("message");
        assertEquals("message", ex.getMessage());
    }

    @Test
    void ensureDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("already exists");
        assertEquals("already exists", ex.getMessage());
    }

    @Test
    void ensureResourceInUseException() {
        ResourceInUseException ex = new ResourceInUseException("resource in use");
        assertEquals("resource in use", ex.getMessage());
    }

    @Test
    void ensureConcurrencyException() {
        ConcurrencyException ex = new ConcurrencyException("concurrency mismatch");
        assertEquals("concurrency mismatch", ex.getMessage());
    }

    @Test
    void ensureInvalidListingCriteriaException() {
        InvalidListingCriteriaException ex = new InvalidListingCriteriaException("invalid criteria");
        assertEquals("invalid criteria", ex.getMessage());
    }

    @Test
    void ensurePaginatedResultRecord() {
        List<String> items = List.of("A", "B");
        PaginatedResult<String> paginatedResult = new PaginatedResult<>(items, 2L);
        assertEquals(items, paginatedResult.data());
        assertEquals(2L, paginatedResult.totalElements());
    }
}
