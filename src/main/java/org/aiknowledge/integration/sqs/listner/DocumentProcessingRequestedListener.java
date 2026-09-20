package org.aiknowledge.integration.sqs.listner;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.integration.sqs.event.DocumentProcessingEvent;
import org.aiknowledge.integration.sqs.publisher.DocumentEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentProcessingRequestedListener {
    private final DocumentEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DocumentProcessingEvent event) {
        eventPublisher.publishProcessingEvent(new DocumentProcessingEvent(event.jobId()));
    }
}
