package org.aiknowledge.repository;

import org.aiknowledge.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findTop6ByConversationIdOrderByCreatedAtDesc(UUID conversationId);
}
