package org.aiknowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.aiknowledge.enums.SourceType;
import org.aiknowledge.integration.rag.CitationMetadata;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "message_citations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "vector_document_id", nullable = false)
    private String vectorDocumentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_name")
    private String sourceName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_metadata", columnDefinition = "jsonb")
    private CitationMetadata sourceMetadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}