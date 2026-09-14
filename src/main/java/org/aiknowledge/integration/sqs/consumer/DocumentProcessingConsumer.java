package org.aiknowledge.integration.sqs.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.integration.processing.DocumentProcessingService;
import org.aiknowledge.integration.sqs.event.DocumentProcessingEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessingConsumer {
    private final DocumentProcessingService documentProcessingService;

    @SqsListener("${app.sqs.document-processing-queue}")
    public void consume(DocumentProcessingEvent event) {
        documentProcessingService.process(event.jobId());
    }
}
