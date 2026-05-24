package aisafe.shared.infrastructure;

import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Auth",
            "Aircraft Models",
            "Aircrafts",
            "Airports",
            "Routes",
            "Maintenance"
    );

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            List<Tag> tags = openApi.getTags();
            if (tags == null) return;
            tags.sort(Comparator.comparingInt(tag -> {
                int idx = TAG_ORDER.indexOf(tag.getName());
                return idx == -1 ? Integer.MAX_VALUE : idx;
            }));
        };
    }
}
