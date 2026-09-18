package org.aiknowledge.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.ApiSuccessResponse;
import org.aiknowledge.dto.PageResponse;
import org.aiknowledge.dto.request.UpdateConversationTitleRequest;
import org.aiknowledge.dto.response.ConversationResponse;
import org.aiknowledge.dto.response.MessageHistoryResponse;
import org.aiknowledge.security.SecurityUserService;
import org.aiknowledge.service.ConversationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final SecurityUserService securityUserService;
    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PageResponse<ConversationResponse>>> allConversations(
            Pageable pageable, HttpServletRequest request) {
        UUID userId = securityUserService.getCurrentUserId();

        PageResponse<ConversationResponse> response = conversationService.allConversations(userId, pageable);

        return ResponseEntity.ok(ApiSuccessResponse.<PageResponse<ConversationResponse>>builder()
                .success(true)
                .message("Conversations fetched successfully.")
                .data(response)
                .path(request.getRequestURI())
                .build()
        );
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiSuccessResponse<MessageHistoryResponse>> getMessages(
            @PathVariable UUID conversationId, @RequestParam(required = false) Instant beforeCreatedAt,
            @RequestParam(required = false) UUID beforeMessageId, HttpServletRequest request
    ) {
        UUID userId = securityUserService.getCurrentUserId();

        MessageHistoryResponse response = conversationService.getMessages(userId,
                conversationId, beforeCreatedAt, beforeMessageId);

        return ResponseEntity.ok(ApiSuccessResponse.<MessageHistoryResponse>builder()
                .success(true)
                .message("Messages fetched successfully.")
                .data(response)
                .path(request.getRequestURI())
                .build()
        );
    }

    @PatchMapping("/{conversationId}/title")
    public ResponseEntity<ApiSuccessResponse<ConversationResponse>> updateTitle(
            @PathVariable UUID conversationId, @Valid @RequestBody UpdateConversationTitleRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = securityUserService.getCurrentUserId();

        ConversationResponse response = conversationService.updateTitle(userId, conversationId, request.title());

        return ResponseEntity.ok(ApiSuccessResponse.<ConversationResponse>builder()
                .success(true)
                .message("Conversation title updated successfully.")
                .data(response)
                .path(httpRequest.getRequestURI())
                .build()
        );
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteConversation(
            @PathVariable UUID conversationId, HttpServletRequest request
    ) {
        UUID userId = securityUserService.getCurrentUserId();

        conversationService.deleteConversation(userId, conversationId);

        return ResponseEntity.ok(ApiSuccessResponse.<Void>builder()
                .success(true)
                .message("Conversation deleted successfully.")
                .data(null)
                .path(request.getRequestURI())
                .build()
        );
    }
}
