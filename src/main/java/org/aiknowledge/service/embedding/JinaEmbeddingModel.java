package org.aiknowledge.service.embedding;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.response.JinaEmbeddingResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JinaEmbeddingModel implements EmbeddingModel {
    private final JinaEmbeddingClient jinaEmbeddingClient;

    @Override
    public float[] embed(Document document) {
        JinaEmbeddingResponse response = jinaEmbeddingClient.embed(List.of(document.getText()));

        if (response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("Jina returned no embedding");
        }

        List<Float> values = response.data().getFirst().embedding();
        float[] vector = new float[values.size()];
        
        for (int i = 0; i < values.size(); i++) {
            vector[i] = values.get(i);
        }

        return vector;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> texts = request.getInstructions();
        JinaEmbeddingResponse response = jinaEmbeddingClient.embed(texts);

        List<Embedding> embeddings = new ArrayList<>();
        response.data()
                .stream()
                .sorted((a, b) -> Integer.compare(
                        a.index(),
                        b.index()
                ))
                .forEach(data -> {

                    List<Float> values = data.embedding();

                    float[] vector = new float[values.size()];

                    for (int i = 0; i < values.size(); i++) {
                        vector[i] = values.get(i);
                    }

                    embeddings.add(
                            new Embedding(
                                    vector,
                                    data.index()
                            )
                    );
                });

        return new EmbeddingResponse(embeddings);
    }
}
