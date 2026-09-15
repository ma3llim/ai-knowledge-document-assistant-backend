package org.aiknowledge.integration.rag;

import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.Message;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DocumentContextBuilder {
    public String build(List<Document> documents, List<Message> history) {
        StringBuilder context = new StringBuilder();

        appendConversationHistory(context, history);
        appendDocumentContext(context, documents);

        return context.toString().trim();
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
            context.append(message.getRole()).append(": ").append(message.getContent()).append("\n\n");
        }
    }

    private void appendDocumentContext(StringBuilder context, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        context.append("""
                DOCUMENT CONTEXT
                ================
                """);

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);

            context.append("--- Chunk ").append(i + 1).append(" ---\n");
            context.append(document.getText()).append("\n\n");
        }
    }
}
