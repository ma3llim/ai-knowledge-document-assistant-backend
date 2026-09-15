package org.aiknowledge.validation;

import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.response.ChatResponseDto;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatResponseValidator {
    public ChatResponseDto validate(ChatResponseDto response) {

        if (response == null) {
            throw new IllegalStateException("LLM returned a null response");
        }

        if (response.answer() == null || response.answer().isBlank()) {
            throw new IllegalStateException("LLM returned an empty answer");
        }

        return response;
    }
}
