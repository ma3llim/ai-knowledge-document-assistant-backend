package org.aiknowledge.integration.query.model;

public record QuerySpec(
        QueryType type,
        QueryIntent intent,
        String searchQuery,
        RetrievalStrategy retrievalStrategy,
        boolean requiresDocuments
) {
}
