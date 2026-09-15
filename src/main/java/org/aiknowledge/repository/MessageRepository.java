package org.aiknowledge.repository;

import org.aiknowledge.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    @Query("""
            SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC
            """)
    List<Message> findRecentMessages(UUID conversationId, Pageable pageable);
}
