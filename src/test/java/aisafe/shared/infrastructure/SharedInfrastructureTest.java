package aisafe.shared.infrastructure;

import aisafe.shared.application.dtos.BulkImportResult;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SharedInfrastructureTest {

    @Test
    void ensureETagUtilsParsesValidETags() {
        assertEquals(5L, ETagUtils.parseVersion("5"));
        assertEquals(5L, ETagUtils.parseVersion("W/\"5\""));
        assertEquals(10L, ETagUtils.parseVersion("\"10\""));
        assertEquals(123L, ETagUtils.parseVersion("W/123"));
    }

    @Test
    void ensureETagUtilsThrowsOnInvalidETags() {
        assertThrows(IllegalArgumentException.class, () -> ETagUtils.parseVersion(null));
        assertThrows(IllegalArgumentException.class, () -> ETagUtils.parseVersion("   "));
        assertThrows(IllegalArgumentException.class, () -> ETagUtils.parseVersion("W/abc"));
    }

    @Test
    void ensureETagUtilsPrivateConstructor() throws Exception {
        Constructor<ETagUtils> constructor = ETagUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    void ensureBulkImportResponseBuilderCreated() {
        BulkImportResult<String> result = new BulkImportResult<>();
        result.addSuccess("Item 1");
        
        ResponseEntity<Map<String, Object>> response = BulkImportResponseBuilder.buildResponse(result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().get("totalProcessed"));
        assertEquals(1, response.getBody().get("successfulCount"));
        assertEquals(0, response.getBody().get("errorCount"));
    }

    @Test
    void ensureBulkImportResponseBuilderPrivateConstructor() throws Exception {
        Constructor<BulkImportResponseBuilder> constructor = BulkImportResponseBuilder.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void ensureBulkImportResponseBuilderMultiStatus() {
        BulkImportResult<String> result = new BulkImportResult<>();
        result.addSuccess("Item 1");
        result.addError(2, "bad data", "format error");
        
        ResponseEntity<Map<String, Object>> response = BulkImportResponseBuilder.buildResponse(result);
        assertEquals(HttpStatus.MULTI_STATUS, response.getStatusCode());
        assertEquals(2, response.getBody().get("totalProcessed"));
        assertEquals(1, response.getBody().get("successfulCount"));
        assertEquals(1, response.getBody().get("errorCount"));
        assertNotNull(response.getBody().get("errors"));
    }

    @Test
    void ensureBulkImportResponseBuilderBadRequest() {
        BulkImportResult<String> result = new BulkImportResult<>();
        result.addError(1, "bad data", "format error");
        
        ResponseEntity<Map<String, Object>> response = BulkImportResponseBuilder.buildResponse(result);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().get("totalProcessed"));
        assertEquals(0, response.getBody().get("successfulCount"));
        assertEquals(1, response.getBody().get("errorCount"));
    }

    @Test
    void ensureBulkImportResultWithNoRowsIsFullySuccessfulFalse() {
        BulkImportResult<String> result = new BulkImportResult<>();
        assertFalse(result.isFullySuccessful());
        assertFalse(result.hasAnySuccess());
    }

    @Test
    void ensureDocsStaticResourceConfigWithSlash() throws Exception {
        DocsStaticResourceConfig config = new DocsStaticResourceConfig();
        java.lang.reflect.Field field = DocsStaticResourceConfig.class.getDeclaredField("docsPath");
        field.setAccessible(true);
        field.set(config, "docs/");

        org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry.class);
        org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration registration = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/docs/**")).thenReturn(registration);

        config.addResourceHandlers(registry);
        verify(registration).addResourceLocations("file:docs/");
    }

    @Test
    void ensureDocsStaticResourceConfigWithoutSlash() throws Exception {
        DocsStaticResourceConfig config = new DocsStaticResourceConfig();
        java.lang.reflect.Field field = DocsStaticResourceConfig.class.getDeclaredField("docsPath");
        field.setAccessible(true);
        field.set(config, "docs");

        org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry.class);
        org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration registration = mock(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/docs/**")).thenReturn(registration);

        config.addResourceHandlers(registry);
        verify(registration).addResourceLocations("file:docs/");
    }

    @Test
    void ensureOpenApiConfigSortsTagsCorrectly() {
        OpenApiConfig config = new OpenApiConfig();
        org.springdoc.core.customizers.OpenApiCustomizer customizer = config.tagOrderCustomizer();
        assertNotNull(customizer);

        // Test with null tags list
        io.swagger.v3.oas.models.OpenAPI openApiWithNullTags = mock(io.swagger.v3.oas.models.OpenAPI.class);
        when(openApiWithNullTags.getTags()).thenReturn(null);
        assertDoesNotThrow(() -> customizer.customise(openApiWithNullTags));

        // Test sorting
        io.swagger.v3.oas.models.OpenAPI openApi = mock(io.swagger.v3.oas.models.OpenAPI.class);
        io.swagger.v3.oas.models.tags.Tag tag1 = new io.swagger.v3.oas.models.tags.Tag().name("Airports");
        io.swagger.v3.oas.models.tags.Tag tag2 = new io.swagger.v3.oas.models.tags.Tag().name("Auth");
        io.swagger.v3.oas.models.tags.Tag tag3 = new io.swagger.v3.oas.models.tags.Tag().name("UnknownTag");
        java.util.List<io.swagger.v3.oas.models.tags.Tag> tags = new java.util.ArrayList<>(java.util.List.of(tag1, tag2, tag3));
        
        when(openApi.getTags()).thenReturn(tags);
        customizer.customise(openApi);

        // Expected order: Auth (index 0), Airports (index 3 in TAG_ORDER, but index 1 here), UnknownTag (MAX_VALUE)
        assertEquals("Auth", tags.get(0).getName());
        assertEquals("Airports", tags.get(1).getName());
        assertEquals("UnknownTag", tags.get(2).getName());
    }
}
