package org.aiknowledge.repository;

import org.aiknowledge.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    long countByUserIdAndDocumentId(UUID userId, UUID documentId);

    Optional<Conversation> findByIdAndUserIdAndDocumentId(UUID conversationId, UUID userId, UUID documentId);
}
