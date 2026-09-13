package org.aiknowledge.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aiknowledge.dto.request.ChatQuestionRequest;
import org.aiknowledge.service.chat.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public void askQuestion(@Valid @RequestBody ChatQuestionRequest chatQuestionRequest) {
        chatService.processQuestion(chatQuestionRequest.documentId(), chatQuestionRequest.query());
    }
}
