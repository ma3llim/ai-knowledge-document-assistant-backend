package org.aiknowledge.integration.sqs.publisher;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.integration.sqs.event.DocumentProcessingEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEventPublisher {
    private final SqsTemplate sqsTemplate;
    private final AppProperties properties;

    public void publishProcessingEvent(DocumentProcessingEvent event) {
        sqsTemplate.send(properties.messaging().sqs().documentProcessingQueue(), event);
    }
}
