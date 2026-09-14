package org.aiknowledge.integration.sqs.event;

import java.util.UUID;

public record DocumentProcessingEvent(
        UUID jobId
) {
}
