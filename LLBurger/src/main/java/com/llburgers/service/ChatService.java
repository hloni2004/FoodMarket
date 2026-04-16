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
    private final boolean aiEnabled;
    private final int menuContextLimit;

    public ChatService(WebClient.Builder webClientBuilder,
                       ProductRepository productRepository,
                       ObjectMapper objectMapper,
                       @Value("${ai.chat.azure.endpoint:}") String azureEndpoint,
                       @Value("${ai.chat.azure.deployment:}") String azureDeployment,
                       @Value("${ai.chat.azure.api-version:2025-01-01-preview}") String azureApiVersion,
                       @Value("${ai.chat.azure.api-key:}") String azureApiKey,
                       @Value("${ai.chat.menu-context-limit:8}") int menuContextLimit) {
        this.webClient = webClientBuilder.build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.azureEndpoint = azureEndpoint == null ? "" : azureEndpoint.trim();
        this.azureDeployment = azureDeployment == null ? "" : azureDeployment.trim();
        this.azureApiVersion = azureApiVersion == null ? "" : azureApiVersion.trim();
        this.azureApiKey = azureApiKey == null ? "" : azureApiKey.trim();
        this.aiEnabled = !this.azureEndpoint.isBlank()
                && !this.azureDeployment.isBlank()
                && !this.azureApiVersion.isBlank()
                && !this.azureApiKey.isBlank();
        if (!aiEnabled) {
            log.warn("[CHAT] Azure OpenAI chat config missing; AI chat functionality is disabled.");
        }
        this.menuContextLimit = menuContextLimit;
    }

    public Mono<String> chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (!aiEnabled) {
            return Mono.error(new IllegalStateException("AI chat functionality is disabled: Azure OpenAI environment variables not configured"));
        }

        String menuContext = buildMenuContext();
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
