package org.aiknowledge.integration.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.entity.Message;
import org.aiknowledge.integration.rag.model.RagContext;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentContextBuilder {
    private final AppProperties appProperties;

    public String build(RagContext ragContext) {

        StringBuilder context = new StringBuilder();

        appendConversationHistory(context, ragContext.conversationHistory());
        appendDocumentContext(context, ragContext.retrievedDocuments());

        return truncateContext(context.toString().trim());
    }

    private void appendConversationHistory(StringBuilder context, List<Message> history) {
        if (history == null || history.isEmpty()) {
            return;
        }

        context.append("""
                CONVERSATION HISTORY
                ====================
                """);

        for (Message message : history) {
            context.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n\n");
        }
    }

    private void appendDocumentContext(StringBuilder context, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents available for context building");
            return;
        }

        context.append("""
                DOCUMENT CONTEXT
                ================
                """);

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);

            String content = limitChunkContent(document.getText());

            context.append("--- Chunk ")
                    .append(i + 1)
                    .append(" ---\n");

            appendMetadata(context, document.getMetadata());

            context.append("Content:\n")
                    .append(content)
                    .append("\n\n");
        }
    }

    private void appendMetadata(StringBuilder context, Map<String, Object> metadata) {
        appendMetadataValue(context, "File", metadata.get("file_name"));
        appendMetadataValue(context, "Page", metadata.get("page_number"));
        appendMetadataValue(context, "Chunk", metadata.get("chunk_index"));
        appendMetadataValue(context, "Section", metadata.get("section_name"));
    }

    private void appendMetadataValue(StringBuilder context, String label, Object value) {
        if (value != null) {
            context.append(label)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }

    private String limitChunkContent(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        int maxCharacters = appProperties.ai().rag().context().maxChunkCharacters();
        if (content.length() <= maxCharacters) {
            return content;
        }

        log.debug("Chunk content truncated. originalCharacters={}, maxCharacters={}", content.length(), maxCharacters);
        return content.substring(0, maxCharacters);
    }

    private String truncateContext(String context) {
        int maxCharacters = appProperties.ai().rag().context().maxContextCharacters();
        if (context.length() <= maxCharacters) {
            return context;
        }

        log.debug("RAG context truncated. originalCharacters={}, maxCharacters={}", context.length(), maxCharacters);

        return context.substring(0, maxCharacters);
    }
}
