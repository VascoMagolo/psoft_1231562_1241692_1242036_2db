package aisafe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DocsStaticResourceConfig implements WebMvcConfigurer {

    @Value("${aisafe.docs.path:docs}")
    private String docsPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = docsPath.endsWith("/") ? "file:" + docsPath : "file:" + docsPath + "/";
        registry.addResourceHandler("/docs/**").addResourceLocations(location);
    }
}
