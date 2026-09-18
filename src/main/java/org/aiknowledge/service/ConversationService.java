package org.aiknowledge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.aiknowledge.config.Constants;
import org.aiknowledge.dto.MessageCursor;
import org.aiknowledge.dto.PageResponse;
import org.aiknowledge.dto.response.ConversationResponse;
import org.aiknowledge.dto.response.MessageHistoryResponse;
import org.aiknowledge.dto.response.MessageResponse;
import org.aiknowledge.entity.Conversation;
import org.aiknowledge.entity.Message;
import org.aiknowledge.enums.MessageRole;
import org.aiknowledge.exception.ResourceNotFoundException;
import org.aiknowledge.repository.ConversationRepository;
import org.aiknowledge.repository.MessageRepository;
import org.aiknowledge.websocket.dto.ConversationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DocumentService documentService;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    public ConversationResult getOrCreateConversation(UUID userId, UUID documentId, UUID conversationId) {
        if (conversationId != null) {
            Conversation conversation = conversationRepository.findByIdAndUserIdAndDocumentId(conversationId, userId, documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

            return new ConversationResult(conversation.getId(), conversation.getTitle(), false);
        }

        Conversation conversation = Conversation.builder()
                .userId(userId)
                .documentId(documentId)
                .title(documentService.getDocumentFileName(userId, documentId))
                .build();

        Conversation savedConversation = conversationRepository.save(conversation);

        return new ConversationResult(savedConversation.getId(), savedConversation.getTitle(), true);
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

    public PageResponse<ConversationResponse> allConversations(UUID userId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);

        List<ConversationResponse> content = conversations.getContent()
                .stream()
                .map(conversation -> objectMapper.convertValue(conversation, ConversationResponse.class))
                .toList();

        return PageResponse.<ConversationResponse>builder()
                .content(content)
                .page(conversations.getNumber())
                .size(conversations.getSize())
                .totalElements(conversations.getTotalElements())
                .totalPages(conversations.getTotalPages())
                .first(conversations.isFirst())
                .last(conversations.isLast())
                .build();
    }

    public MessageHistoryResponse getMessages(UUID userId, UUID conversationId, Instant beforeCreatedAt, UUID beforeMessageId) {
        validateConversationAccess(userId, conversationId);

        PageRequest pageable = PageRequest.of(0, Constants.MESSAGE_PAGE_SIZE);
        Slice<Message> messages;

        if (beforeCreatedAt == null || beforeMessageId == null) {
            messages = messageRepository.findByConversationIdOrderByCreatedAtDescIdDesc(conversationId, pageable);
        } else {
            messages = messageRepository.findMessagesBefore(conversationId, beforeCreatedAt, beforeMessageId, pageable);
        }

        List<MessageResponse> content = messages.getContent().stream()
                .map(message -> objectMapper.convertValue(message, MessageResponse.class))
                .toList();

        MessageCursor nextCursor = null;
        if (!content.isEmpty() && messages.hasNext()) {
            Message lastMessage = messages.getContent().get(messages.getContent().size() - 1);

            nextCursor = MessageCursor.builder()
                    .beforeCreatedAt(lastMessage.getCreatedAt())
                    .beforeMessageId(lastMessage.getId())
                    .build();
        }

        return MessageHistoryResponse.builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasMore(messages.hasNext())
                .build();
    }

    private void validateConversationAccess(UUID userId, UUID conversationId) {
        boolean exists = conversationRepository.existsByIdAndUserId(conversationId, userId);

        if (!exists) {
            log.warn("Conversation access denied or conversation not found. conversationId={}, userId={}",
                    conversationId, userId);
            throw new ResourceNotFoundException("Conversation not found");
        }
    }

    public ConversationResponse updateTitle(UUID userId, UUID conversationId, String title) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId).orElseThrow(() -> {
            log.warn("Update conversation title failed: conversation not found. conversationId={}, userId={}",
                    conversationId, userId);
            return new ResourceNotFoundException("Conversation not found");
        });

        conversation.setTitle(title.trim());

        Conversation savedConversation = conversationRepository.save(conversation);

        return objectMapper.convertValue(savedConversation, ConversationResponse.class);
    }

    public void deleteConversation(UUID userId, UUID conversationId) {
        boolean exists = conversationRepository.existsByIdAndUserId(conversationId, userId);

        if (!exists) {
            log.warn("Delete conversation failed: conversation not found. conversationId={}, userId={}",
                    conversationId, userId);
            throw new ResourceNotFoundException("Conversation not found");
        }

        conversationRepository.deleteById(conversationId);
    }

    public List<Message> getRecentHistory(UUID conversationId) {
        int maxTurns = appProperties.ai().rag().context().maxConversationTurns();
        int maxMessages = maxTurns * 2;

        List<Message> messages = messageRepository.findRecentMessages(conversationId, PageRequest.of(0, maxMessages));

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
