package org.aiknowledge.dto.request;

import java.util.UUID;

public record ChatQuestionRequest(UUID documentId, String query) {
}
