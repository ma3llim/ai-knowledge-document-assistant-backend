package org.aiknowledge.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sqs")
public record AwsSqsProperties(
        String documentProcessingQueue,
        String documentSummaryQueue
) {
}