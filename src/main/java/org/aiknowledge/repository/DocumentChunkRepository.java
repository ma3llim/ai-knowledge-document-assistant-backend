package org.aiknowledge.repository;

import org.aiknowledge.entity.DocumentChunk;
import org.aiknowledge.projection.SimilarChunkProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    @Query(
            value = """
                    SELECT
                                        id,
                                        document_id AS documentId,
                                        chunk_index AS chunkIndex,
                                        content,
                                        page_number AS pageNumber,
                                        section_name AS sectionName,
                                        sheet_name AS sheetName,
                                        slide_number AS slideNumber,
                                                            1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
                                        FROM document_chunks
                     WHERE document_id = :documentId
                     ORDER BY embedding <=> CAST(:queryVector AS vector) LIMIT :limit
                    """,
            nativeQuery = true)
    List<SimilarChunkProjection> findSimilarChunks(
            @Param("documentId") UUID documentId,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);

    @Modifying
    @Query("""
            DELETE FROM DocumentChunk dc WHERE dc.documentId = :documentId
            """)
    void deleteAllByDocumentId(@Param("documentId") UUID documentId);
}
