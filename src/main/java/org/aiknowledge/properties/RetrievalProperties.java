package org.aiknowledge.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rag.retrieval")
public class RetrievalProperties {
    private double similarityThreshold = 0.80;
    private int topK = 5;
}
