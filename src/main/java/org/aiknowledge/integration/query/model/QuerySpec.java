package org.aiknowledge.integration.query.model;

public record QuerySpec(
        QueryType type,
        String searchQuery,
        RetrievalStrategy retrievalStrategy
) {
}
