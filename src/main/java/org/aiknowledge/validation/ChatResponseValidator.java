package org.aiknowledge.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatResponseValidator {
    public String validate(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("LLM returned an empty answer");
        }

        return response;
    }
}
