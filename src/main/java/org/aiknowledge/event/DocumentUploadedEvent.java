package org.aiknowledge.event;

import java.util.UUID;

public record DocumentUploadedEvent(UUID jobId) {
}
