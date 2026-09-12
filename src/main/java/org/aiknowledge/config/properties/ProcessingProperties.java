package org.aiknowledge.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.processing")
public class ProcessingProperties {
    private int chunkSize;
    private int chunkOverlap;
}