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
import reactor.core.publisher.Mono;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
    private final double temperature;
    private final int menuContextLimit;

    public ChatService(WebClient.Builder webClientBuilder,
                       ProductRepository productRepository,
                       ObjectMapper objectMapper,
                       @Value("${ai.chat.base-url:https://router.huggingface.co/v1}") String baseUrl,
                       @Value("${ai.chat.api-key:}") String apiKey,
                       @Value("${ai.chat.model:moonshotai/Kimi-K2-Instruct-0905}") String model,
                       @Value("${ai.chat.temperature:0.5}") double temperature,
                       @Value("${ai.chat.menu-context-limit:8}") int menuContextLimit) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI chat service is not configured: missing API key");
        }
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.menuContextLimit = menuContextLimit;
    }

    public Mono<String> chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String menuContext = buildMenuContext();

        String payload;
        try {
            payload = objectMapper.createObjectNode()
                    .put("model", model)
                    .put("temperature", temperature)
                    .set("messages", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "system")
                                    .put("content", SYSTEM_PROMPT + "\n\nCurrent menu snapshot:\n" + menuContext))
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", message)))
                    .toString();
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Unable to prepare AI request"));
        }

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
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
