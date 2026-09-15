package org.aiknowledge.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.entity.MessageCitation;
import org.aiknowledge.enums.SourceType;
import org.aiknowledge.integration.rag.CitationMetadata;
import org.aiknowledge.repository.MessageCitationRepository;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CitationService {
    private final MessageCitationRepository messageCitationRepository;
    private final ObjectMapper objectMapper;

    public void saveCitations(UUID messageId, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.debug("No documents available for citation. messageId={}", messageId);
            return;
        }

        List<MessageCitation> citations = new ArrayList<>();

        for (Document document : documents) {
            Map<String, Object> metadata = document.getMetadata();
            UUID documentId = parseUuid(metadata.get("document_id"));

            if (documentId == null) {
                log.warn("Skipping citation because document_id is missing. messageId={}", messageId);
                continue;
            }

            String vectorDocumentId = document.getId();

            SourceType sourceType = resolveSourceType(metadata.get("content_type"));

            CitationMetadata citationMetadata = new CitationMetadata(
                    getString(metadata, "file_name"),
                    getString(metadata, "content_type"),
                    getInteger(metadata, "chunk_index"),
                    getInteger(metadata, "page_number"),
                    getString(metadata, "section_name"),
                    getString(metadata, "sheet_name"),
                    getInteger(metadata, "slide_number")
            );

            MessageCitation citation = MessageCitation.builder()
                    .messageId(messageId)
                    .documentId(documentId)
                    .vectorDocumentId(vectorDocumentId)
                    .sourceType(sourceType)
                    .sourceName(getString(metadata, "file_name"))
                    .sourceMetadata(citationMetadata)
                    .build();

            citations.add(citation);
        }

        if (!citations.isEmpty()) {
            messageCitationRepository.saveAll(citations);
            log.info("Saved {} citations. messageId={}", citations.size(), messageId);
        }
    }

    private String convertToJson(CitationMetadata metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            log.error("Failed to convert citation metadata to JSON", exception);
            throw new IllegalStateException("Failed to serialize citation metadata", exception);
        }
    }

    private SourceType resolveSourceType(Object contentType) {
        if (contentType == null) {
            return SourceType.UNKNOWN;
        }

        String type = contentType.toString().trim().toUpperCase();

        try {
            return SourceType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            return SourceType.UNKNOWN;
        }
    }

    private String getString(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getInteger(Map<String, Object> metadata, String key) {
        Object value = metadata.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
