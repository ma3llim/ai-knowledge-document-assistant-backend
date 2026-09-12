package org.aiknowledge.dto.request;

import java.util.List;

public record JinaEmbeddingRequest(
        String model,
        String task,
        List<String> input
) {
}