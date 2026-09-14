package org.aiknowledge.projection;

import java.util.UUID;

public interface DocumentChunkContentProjection {
    UUID getId();

    Integer getChunkIndex();

    String getContent();
}