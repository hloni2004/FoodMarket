package com.llburgers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.llburgers.domain.Product;
import com.llburgers.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant for an online food ordering platform.
            Help users choose meals, answer questions, and provide recommendations based on budget and preferences.
            Keep responses friendly and concise.
            """;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final String apiKey;
    private final String model;

    public ChatService(WebClient.Builder webClientBuilder,
                       ProductRepository productRepository,
                       ObjectMapper objectMapper,
                       @Value("${ai.chat.base-url:https://router.huggingface.co/v1}") String baseUrl,
                       @Value("${ai.chat.api-key:}") String apiKey,
                       @Value("${ai.chat.model:moonshotai/Kimi-K2-Instruct-0905}") String model) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI chat service is not configured");
        }

        String menuContext = buildMenuContext();

        String payload;
        try {
            payload = objectMapper.createObjectNode()
                    .put("model", model)
                    .put("temperature", 0.5)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "system")
                                    .put("content", SYSTEM_PROMPT + "\n\nCurrent menu snapshot:\n" + menuContext))
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", message)))
                    .toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to prepare AI request");
        }

        String responseBody;
        try {
            responseBody = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new IllegalStateException("AI provider request failed with status " + ex.getStatusCode().value());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to reach AI provider");
        }

        return extractReply(responseBody);
    }

    private String extractReply(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            String content = contentNode.asText("");
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

        StringBuilder builder = new StringBuilder();
        products.stream()
                .limit(8)
                .forEach(product -> builder.append("- ")
                        .append(product.getName())
                        .append(" (")
                        .append(product.getCategory())
                        .append(") - ")
                        .append(product.getPrice())
                        .append('\n'));
        return builder.toString().trim();
    }
}
