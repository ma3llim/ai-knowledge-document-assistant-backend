package org.aiknowledge.integration.sqs.publisher;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.integration.sqs.event.DocumentProcessingEvent;
import org.aiknowledge.properties.AwsSqsProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentEventPublisher {
    private final SqsTemplate sqsTemplate;
    private final AwsSqsProperties properties;

    public void publishProcessingEvent(DocumentProcessingEvent event) {
        sqsTemplate.send(properties.documentProcessingQueue(), event);
    }
}
