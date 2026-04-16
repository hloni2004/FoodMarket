package com.llburgers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llburgers.domain.Product;
import com.llburgers.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final String SYSTEM_PROMPT = """
            You are an AI assistant for an online food ordering platform.
            Help users choose meals, answer questions, and provide recommendations based on budget and preferences.
            Keep responses friendly and concise.
            """;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final String endpoint;
    private final String hfToken;
    private final boolean aiEnabled;
    private final int menuContextLimit;

    public ChatService(WebClient.Builder webClientBuilder,
                       ProductRepository productRepository,
                       ObjectMapper objectMapper,
                       @Value("${ai.chat.endpoint:https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2}") String endpoint,
                       @Value("${HF_TOKEN:}") String hfToken,
                       @Value("${ai.chat.menu-context-limit:8}") int menuContextLimit) {
        this.webClient = webClientBuilder.build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.endpoint = endpoint;
        this.hfToken = hfToken == null ? "" : hfToken.trim();
        this.aiEnabled = !this.hfToken.isBlank();
        if (!aiEnabled) {
            log.warn("[CHAT] HF_TOKEN not configured; AI chat functionality is disabled.");
        }
        this.menuContextLimit = menuContextLimit;
    }

    public Mono<String> chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (!aiEnabled) {
            return Mono.error(new IllegalStateException("AI chat functionality is disabled"));
        }

        String menuContext = buildMenuContext();

        String payload;
        try {
            payload = objectMapper.createObjectNode()
                    .put("inputs", SYSTEM_PROMPT
                            + "\n\nCurrent menu snapshot:\n"
                            + menuContext
                            + "\n\nUser: "
                            + message
                            + "\nAssistant:")
                    .toString();
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Unable to prepare AI request"));
        }

        return webClient.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + hfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractReply)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new IllegalStateException("AI provider request failed with status " + ex.getStatusCode().value()))
                .onErrorMap(ex -> !(ex instanceof IllegalStateException),
                        ex -> new IllegalStateException("Unable to reach AI provider"));
    }

    private String extractReply(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.hasNonNull("error")) {
                throw new IllegalStateException("AI provider returned an error");
            }
            String content = "";
            if (root.isArray() && !root.isEmpty()) {
                content = root.path(0).path("generated_text").asText("");
            } else if (root.isObject()) {
                content = root.path("generated_text").asText("");
            }
            if (content.isBlank()) {
                throw new IllegalStateException("AI provider returned an empty response");
            }
            return content.trim();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse AI response");
        }
    }

    private String buildMenuContext() {
        List<Product> products = productRepository.findByAvailabilityAndDeletedFalse(true);
        if (products.isEmpty()) {
            return "No menu items are currently available.";
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        StringBuilder builder = new StringBuilder();
        products.stream()
                .limit(menuContextLimit)
                .forEach(product -> builder.append("- ")
                        .append(product.getName())
                        .append(" (")
                        .append(product.getCategory())
                        .append(") - ")
                        .append(currencyFormatter.format(product.getPrice()))
                        .append('\n'));
        return builder.toString().trim();
    }
}
