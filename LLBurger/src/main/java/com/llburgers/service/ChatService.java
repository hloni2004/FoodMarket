package com.llburgers.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final String azureEndpoint;
    private final String azureDeployment;
    private final String azureApiVersion;
    private final String azureApiKey;
    private final String geminiEndpoint;
    private final String geminiModel;
    private final String geminiApiKey;
    private final boolean azureEnabled;
    private final boolean geminiEnabled;
    private final boolean aiEnabled;
    private final int menuContextLimit;

    public ChatService(WebClient.Builder webClientBuilder,
                       ProductRepository productRepository,
                       ObjectMapper objectMapper,
                       @Value("${ai.chat.azure.endpoint:}") String azureEndpoint,
                       @Value("${ai.chat.azure.deployment:}") String azureDeployment,
                       @Value("${ai.chat.azure.api-version:2025-01-01-preview}") String azureApiVersion,
                       @Value("${ai.chat.azure.api-key:}") String azureApiKey,
                       @Value("${ai.chat.gemini.endpoint:}") String geminiEndpoint,
                       @Value("${ai.chat.gemini.model:}") String geminiModel,
                       @Value("${ai.chat.gemini.api-key:}") String geminiApiKey,
                       @Value("${ai.chat.menu-context-limit:8}") int menuContextLimit) {
        this.webClient = webClientBuilder.build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.azureEndpoint = azureEndpoint == null ? "" : azureEndpoint.trim();
        this.azureDeployment = azureDeployment == null ? "" : azureDeployment.trim();
        this.azureApiVersion = azureApiVersion == null ? "" : azureApiVersion.trim();
        this.azureApiKey = azureApiKey == null ? "" : azureApiKey.trim();
        this.geminiEndpoint = (geminiEndpoint == null || geminiEndpoint.isBlank())
                ? "https://api.openai.google/v1"
                : geminiEndpoint.trim();
        this.geminiModel = geminiModel == null ? "" : geminiModel.trim();
        this.geminiApiKey = geminiApiKey == null ? "" : geminiApiKey.trim();
        this.azureEnabled = !this.azureEndpoint.isBlank()
                && !this.azureDeployment.isBlank()
                && !this.azureApiVersion.isBlank()
                && !this.azureApiKey.isBlank();
        this.geminiEnabled = !this.geminiModel.isBlank()
                && !this.geminiApiKey.isBlank();
        this.aiEnabled = this.azureEnabled || this.geminiEnabled;
        if (!aiEnabled) {
            log.warn("[CHAT] No AI chat provider is configured; AI chat functionality is disabled.");
        } else if (azureEnabled && geminiEnabled) {
            log.info("[CHAT] Both Azure and Gemini configured; using Gemini by default.");
        }
        this.menuContextLimit = menuContextLimit;
    }

    public Mono<String> chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (!aiEnabled) {
            return Mono.error(new IllegalStateException("AI chat functionality is disabled: no chat provider configured"));
        }

        String menuContext = buildMenuContext();
        if (geminiEnabled) {
            return chatWithGemini(message, menuContext);
        }
        return chatWithAzure(message, menuContext);
    }

    private Mono<String> chatWithGemini(String message, String menuContext) {
        String endpoint = buildGeminiChatEndpoint();

        String payload;
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", geminiModel);
            ArrayNode messages = requestBody.putArray("messages");
            messages.addObject()
                    .put("author", "system")
                    .set("content", objectMapper.createObjectNode().put("text", SYSTEM_PROMPT + "\n\nCurrent menu snapshot:\n" + menuContext));
            messages.addObject()
                    .put("author", "user")
                    .set("content", objectMapper.createObjectNode().put("text", message));
            payload = requestBody.toString();
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Unable to prepare AI request"));
        }

        return webClient.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractGeminiReply)
                .onErrorMap(WebClientResponseException.class, ex ->
                        new IllegalStateException("AI provider request failed with status " + ex.getStatusCode().value()))
                .onErrorMap(ex -> !(ex instanceof IllegalStateException),
                        ex -> new IllegalStateException("Unable to reach AI provider"));
    }

    private Mono<String> chatWithAzure(String message, String menuContext) {
        String endpoint = buildAzureChatEndpoint();

        String payload;
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode messages = requestBody.putArray("messages");
            messages.addObject()
                    .put("role", "system")
                    .put("content", SYSTEM_PROMPT + "\n\nCurrent menu snapshot:\n" + menuContext);
            messages.addObject()
                    .put("role", "user")
                    .put("content", message);
            payload = requestBody.toString();
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Unable to prepare AI request"));
        }

        return webClient.post()
                .uri(endpoint)
                .header("api-key", azureApiKey)
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

    private String extractGeminiReply(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.hasNonNull("error")) {
                JsonNode errorNode = root.path("error");
                String providerError = errorNode.path("message").asText(errorNode.asText("unknown error"));
                throw new IllegalStateException("AI provider returned an error: " + providerError);
            }
            JsonNode candidate = root.path("candidates").path(0);
            String content = candidate.path("content").path("text").asText("");
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

    private String extractReply(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.hasNonNull("error")) {
                JsonNode errorNode = root.path("error");
                String providerError = errorNode.path("message").asText(errorNode.asText("unknown error"));
                throw new IllegalStateException("AI provider returned an error: " + providerError);
            }
            String content = root.path("choices").path(0).path("message").path("content").asText("");
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

    private String buildAzureChatEndpoint() {
        String normalizedEndpoint = azureEndpoint.endsWith("/")
                ? azureEndpoint.substring(0, azureEndpoint.length() - 1)
                : azureEndpoint;
        String encodedApiVersion = URLEncoder.encode(azureApiVersion, StandardCharsets.UTF_8);
        return normalizedEndpoint
                + "/openai/deployments/"
                + azureDeployment
                + "/chat/completions?api-version="
                + encodedApiVersion;
    }

    private String buildGeminiChatEndpoint() {
        String normalizedEndpoint = geminiEndpoint.endsWith("/")
                ? geminiEndpoint.substring(0, geminiEndpoint.length() - 1)
                : geminiEndpoint;
        return normalizedEndpoint + "/v1/models/" + URLEncoder.encode(geminiModel, StandardCharsets.UTF_8) + ":generateMessage";
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
