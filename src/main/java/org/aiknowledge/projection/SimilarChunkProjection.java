package org.aiknowledge.projection;

import java.util.UUID;

public interface SimilarChunkProjection {
    UUID getId();

    UUID getDocumentId();

    Integer getChunkIndex();

    String getContent();

    Integer getPageNumber();

    String getSectionName();

    String getSheetName();

    Integer getSlideNumber();

    Double getSimilarity();
}
