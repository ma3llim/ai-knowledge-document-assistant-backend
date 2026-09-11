package org.aiknowledge.event;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.service.DocumentProcessingAsyncService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentUploadedEventListener {
    private final DocumentProcessingAsyncService documentProcessingAsyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDocumentUploaded(DocumentUploadedEvent event) {
        documentProcessingAsyncService.processAsync(event.documentId());
    }
}
