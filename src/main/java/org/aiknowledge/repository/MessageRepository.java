package org.aiknowledge.repository;

import org.aiknowledge.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Slice<Message> findByConversationIdOrderByCreatedAtDescIdDesc(UUID conversationId, Pageable pageable);

    @Query("""
            SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC
            """)
    List<Message> findRecentMessages(UUID conversationId, Pageable pageable);

    @Query("""
            SELECT m FROM Message m WHERE m.conversationId = :conversationId AND ( m.createdAt < :beforeCreatedAt OR
                        (m.createdAt = :beforeCreatedAt AND m.id < :beforeMessageId))""")
    Slice<Message> findMessagesBefore(
            @Param("conversationId") UUID conversationId, @Param("beforeCreatedAt") Instant beforeCreatedAt,
            @Param("beforeMessageId") UUID beforeMessageId, Pageable pageable
    );
}
