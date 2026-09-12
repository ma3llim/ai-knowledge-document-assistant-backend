package org.aiknowledge.dto.response;

import java.util.List;

public record JinaEmbeddingResponse(List<JinaEmbeddingData> data) {
    public record JinaEmbeddingData(int index, List<Float> embedding) {
    }
}
