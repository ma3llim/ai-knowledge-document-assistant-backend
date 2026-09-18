package org.aiknowledge.repository;

import org.aiknowledge.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndUserIdAndDocumentId(UUID conversationId, UUID userId, UUID documentId);

    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID conversationId, UUID userId);

    Optional<Conversation> findByIdAndUserId(UUID conversationId, UUID userId);
    
    List<Conversation> findAllByDocumentIdAndUserId(UUID documentId, UUID userId);
}
