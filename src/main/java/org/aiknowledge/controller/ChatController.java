//package org.aiknowledge.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.aiknowledge.dto.request.ChatQuestionRequest;
//import org.aiknowledge.dto.response.ChatApiResponse;
//import org.aiknowledge.service.ChatService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/chats")
//@RequiredArgsConstructor
//public class ChatController {
//    private final ChatService chatService;
//
//    @GetMapping
//    public ChatApiResponse askQuestion(@Valid @RequestBody ChatQuestionRequest chatQuestionRequest) {
//        return chatService.processQuestion(chatQuestionRequest);
//    }
//}
