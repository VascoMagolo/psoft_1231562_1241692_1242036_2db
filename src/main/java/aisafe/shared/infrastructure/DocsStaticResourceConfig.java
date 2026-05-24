package aisafe.shared.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to serve static resources for API documentation from a configurable file system path.
 */
@Configuration
public class DocsStaticResourceConfig implements WebMvcConfigurer {

    @Value("${aisafe.docs.path:docs}")
    private String docsPath;

    /**
     * Adds a resource handler to serve static files from the specified file system path under the "/docs/**" URL pattern.
     * @param registry the resource handler registry to which the handler will be added
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = docsPath.endsWith("/") ? "file:" + docsPath : "file:" + docsPath + "/";
        registry.addResourceHandler("/docs/**").addResourceLocations(location);
    }
}
