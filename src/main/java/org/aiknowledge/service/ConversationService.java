package org.aiknowledge.service;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.Constants;
import org.aiknowledge.entity.Conversation;
import org.aiknowledge.entity.Message;
import org.aiknowledge.enums.MessageRole;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.repository.ConversationRepository;
import org.aiknowledge.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DocumentService documentService;

    public UUID getOrCreateConversation(UUID userId, UUID documentId, UUID conversationId) {
        if (conversationId != null) {
            Conversation conversation = conversationRepository.findByIdAndUserIdAndDocumentId(conversationId, userId, documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            return conversation.getId();
        }

        long conversationCount = conversationRepository.countByUserIdAndDocumentId(userId, documentId);

        if (conversationCount >= 3) {
            throw new IllegalStateException("Maximum 3 conversations are allowed for this document");
        }

        Conversation conversation = Conversation.builder()
                .userId(userId)
                .documentId(documentId)
                .title(documentService.getDocumentFileName(userId, documentId))
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        return savedConversation.getId();
    }

    public void saveUserMessage(UUID conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(content)
                .build();

        messageRepository.save(message);
    }

    public void saveAssistantMessage(UUID conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .build();

        messageRepository.save(message);
    }

    public List<Message> getMessages(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Message> getRecentHistory(UUID conversationId) {
        List<Message> messages = messageRepository.findTop6ByConversationIdOrderByCreatedAtDesc(conversationId);

        if (messages.isEmpty()) {
            return List.of();
        }

        Collections.reverse(messages);

        return limitHistory(messages);
    }

    private List<Message> limitHistory(List<Message> messages) {
        List<Message> result = new ArrayList<>();
        int totalCharacters = 0;

        // Start from the newest message and work backwards.
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);

            int messageLength = message.getContent().length();

            if (totalCharacters + messageLength > Constants.MAX_HISTORY_CHARACTERS) {
                break;
            }

            result.add(message);
            totalCharacters += messageLength;
        }

        Collections.reverse(result);
        return result;
    }
}
