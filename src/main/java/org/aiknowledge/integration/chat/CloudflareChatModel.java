package org.aiknowledge.integration.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.config.AppProperties;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Primary
@Slf4j
@RequiredArgsConstructor
@Component("chatModel")
public class CloudflareChatModel implements ChatModel {
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    private WebClient webClient() {
        if (webClient == null) {
            AppProperties.Ai.Chat.Cloudflare cloudflare = properties.ai().chat().cloudflare();
            this.webClient = WebClient.builder()
                    .baseUrl(cloudflare.baseUrl())
                    .defaultHeader("Authorization", "Bearer " + cloudflare.apiKey())
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }

        return webClient;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        String response = stream(prompt)
                .map(responseChunk -> {
                    Generation generation = responseChunk.getResult();

                    if (generation == null || generation.getOutput() == null) {
                        return "";
                    }

                    return generation.getOutput().getText();
                })
                .filter(text -> text != null && !text.isEmpty())
                .collectList()
                .map(parts -> String.join("", parts))
                .block();

        if (response == null) {
            response = "";
        }

        AssistantMessage assistantMessage = new AssistantMessage(response);

        Generation generation = new Generation(assistantMessage);

        return new ChatResponse(List.of(generation));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        AppProperties.Ai.Chat.Cloudflare cloudflare = properties.ai().chat().cloudflare();

        Map<String, Object> requestBody = Map.of(
                "model", cloudflare.model(),
                "messages", convertMessages(prompt),
                "temperature", cloudflare.temperature(),
                "max_tokens", cloudflare.maxTokens(),
                "stream", true
        );

        return webClient()
                .post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                })
                .doOnSubscribe(subscription ->
                        log.debug("Starting Cloudflare streaming request"))
                .doOnNext(event ->
                        log.trace("Cloudflare SSE event: {}", event.data()))
                .flatMap(this::parseSseEvent)
                .doOnError(error ->
                        log.error("Cloudflare streaming failed", error));
    }

    private Flux<ChatResponse> parseSseEvent(ServerSentEvent<?> event) {
        if (event == null || event.data() == null) {
            return Flux.empty();
        }

        String data = event.data().toString();

        if (data.isBlank() || "[DONE]".equals(data.trim())) {
            return Flux.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                return Flux.empty();
            }

            JsonNode delta = choices.get(0).path("delta");
            JsonNode content = delta.path("content");

            if (!content.isTextual()) {
                return Flux.empty();
            }

            String text = content.asText();

            if (text.isEmpty()) {
                return Flux.empty();
            }

            log.debug("Cloudflare content chunk: [{}]", text);

            AssistantMessage assistantMessage = new AssistantMessage(text);
            Generation generation = new Generation(assistantMessage);

            return Flux.just(new ChatResponse(List.of(generation)));
        } catch (Exception e) {
            log.warn("Failed to parse Cloudflare SSE event: {}", data, e);
            return Flux.empty();
        }
    }

    private List<Map<String, String>> convertMessages(Prompt prompt) {
        List<Map<String, String>> messages = new ArrayList<>();

        for (Message message : prompt.getInstructions()) {
            String role = switch (message.getMessageType()) {
                case SYSTEM -> "system";
                case ASSISTANT -> "assistant";
                default -> "user";
            };

            messages.add(Map.of("role", role, "content", message.getText()));
        }

        return messages;
    }

    @Override
    public ChatOptions getOptions() {
        AppProperties.Ai.Chat.Cloudflare cloudflare = properties.ai().chat().cloudflare();

        return ChatOptions.builder()
                .model(cloudflare.model())
                .temperature(cloudflare.temperature())
                .maxTokens(cloudflare.maxTokens())
                .build();
    }
}
