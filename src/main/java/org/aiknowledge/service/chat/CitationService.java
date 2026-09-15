package org.aiknowledge.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.response.CitationReference;
import org.aiknowledge.dto.response.CitationResponse;
import org.aiknowledge.entity.MessageCitation;
import org.aiknowledge.enums.SourceType;
import org.aiknowledge.integration.rag.model.CitationMetadata;
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

    public List<CitationResponse> resolve(List<CitationReference> citations, List<Document> documents) {
        if (citations == null || citations.isEmpty()) {
            return List.of();
        }

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return citations.stream()
                .map(CitationReference::source)
                .distinct()
                .filter(source -> source >= 1 && source <= documents.size())
                .map(source -> documents.get(source - 1))
                .map(this::toCitationResponse)
                .filter(this::hasUsefulMetadata)
                .toList();
    }

    private boolean hasUsefulMetadata(CitationResponse citation) {
        return citation.fileName() != null
                || citation.pageNumber() != null
                || citation.sectionName() != null
                || citation.sheetName() != null
                || citation.slideNumber() != null;
    }

    private CitationResponse toCitationResponse(Document document) {

        Map<String, Object> metadata = document.getMetadata();

        log.info("Citation document id={}", document.getId());
        log.info("Citation metadata={}", metadata);

        return new CitationResponse(
                getString(metadata, "file_name"),
                getInteger(metadata, "page_number"),
                getString(metadata, "section_name"),
                getString(metadata, "sheet_name"),
                getInteger(metadata, "slide_number")
        );
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