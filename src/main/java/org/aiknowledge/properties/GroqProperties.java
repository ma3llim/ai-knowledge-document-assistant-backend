package org.aiknowledge.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.groq")
public class GroqProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
}
