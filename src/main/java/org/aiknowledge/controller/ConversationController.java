package org.aiknowledge.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.ApiSuccessResponse;
import org.aiknowledge.dto.PageResponse;
import org.aiknowledge.dto.response.ConversationResponse;
import org.aiknowledge.security.SecurityUserService;
import org.aiknowledge.service.ConversationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
