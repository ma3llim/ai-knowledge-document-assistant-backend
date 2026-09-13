package org.aiknowledge.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.query-specifier")
public record QuerySpecifierProperties(
        Provider cloudflare
) {
    public record Provider(
            String apiKey,
            String baseUrl,
            String model
    ) {
    }
}
