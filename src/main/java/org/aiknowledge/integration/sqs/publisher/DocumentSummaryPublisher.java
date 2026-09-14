package org.aiknowledge.integration.sqs.publisher;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.integration.sqs.event.DocumentSummaryEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentSummaryPublisher {
    private final SqsTemplate sqsTemplate;
    private final AppProperties properties;

    public void publish(DocumentSummaryEvent event) {
        sqsTemplate.send(properties.messaging().sqs().documentSummaryQueue(), event);
    }
}
