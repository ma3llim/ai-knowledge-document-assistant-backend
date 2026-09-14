package org.aiknowledge.integration.sqs.consumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.integration.processing.DocumentSummaryService;
import org.aiknowledge.integration.sqs.event.DocumentSummaryEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentSummaryConsumer {
    private final DocumentSummaryService documentSummaryService;

    @SqsListener("${app.sqs.document-summary-queue}")
    public void consume(DocumentSummaryEvent event) {
        documentSummaryService.processSummary(event.jobId());
    }
}
