package org.aiknowledge.integration.document;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DocumentContentNormalizer {
    public List<Document> normalize(List<Document> documents) {
        return documents.stream().map(this::normalize).toList();
    }

    private Document normalize(Document document) {
        String text = document.getText();

        if (text == null || text.isBlank()) {
            return document;
        }

        String normalizedText = text
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\n *", "\n")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        Map<String, Object> metadata = new HashMap<>();
        document.getMetadata().forEach((key, value) -> {
            if (value != null) {
                metadata.put(key, value);
            }
        });

        return document.mutate().text(normalizedText).metadata(metadata).build();
    }
}
