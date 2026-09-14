package org.aiknowledge.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.summarization.openrouter")
public record SummarizationProperties(
        String apiKey,
        String baseUrl,
        String model,
        Double temperature,
        Integer maxTokens
) {
}
