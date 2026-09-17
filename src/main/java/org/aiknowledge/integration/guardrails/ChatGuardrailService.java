package org.aiknowledge.integration.guardrails;

import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.GuardrailResult;
import org.aiknowledge.integration.prompts.ChatGuardrailPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatGuardrailService {
    private final ChatClient guardrailChatModel;

    public ChatGuardrailService(@Qualifier("guardrailChatModel") OpenAiChatModel guardrailChatModel) {
        this.guardrailChatModel = ChatClient.builder(guardrailChatModel).build();
    }

    public GuardrailResult validateInput(String userQuery) {
        String prompt = ChatGuardrailPrompt.build(userQuery);

        String response = guardrailChatModel
                .prompt()
                .user(prompt)
                .call()
                .content();

        String result = response == null ? "" : response.trim().toLowerCase();

        if ("unsafe".equals(result)) {
            log.warn("Input rejected by guardrail.");
            return GuardrailResult.inputRejected();
        }

        if ("safe".equals(result)) {
            return GuardrailResult.success();
        }

        return GuardrailResult.invalidResponse();
    }
}
