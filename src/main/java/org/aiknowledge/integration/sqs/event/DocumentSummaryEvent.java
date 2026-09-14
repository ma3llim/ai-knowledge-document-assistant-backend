package org.aiknowledge.integration.sqs.event;

import java.util.UUID;

public record DocumentSummaryEvent(
        UUID jobId
) {
}
