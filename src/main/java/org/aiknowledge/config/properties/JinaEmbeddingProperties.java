package org.aiknowledge.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.embedding.jina")
public class JinaEmbeddingProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
}
