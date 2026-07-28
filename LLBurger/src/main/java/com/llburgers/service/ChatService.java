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
            // sensible defaults for Gemini
            requestBody.put("temperature", 0.2);
            requestBody.put("maxOutputTokens", 512);

            ArrayNode messages = requestBody.putArray("messages");

            // Use an explicit system prompt for Gemini (do not alter global SYSTEM_PROMPT used by Azure)
            String geminiSystemPrompt = "You are an AI assistant for an online burger ordering platform.\n\n"
                + "Help customers:\n"
                + "- choose burgers\n"
                + "- compare meals\n"
                + "- answer menu questions\n"
                + "- recommend food\n"
                + "- recommend based on budget\n"
                + "- answer allergy questions when possible\n\n"
                + "Keep responses short, friendly and helpful.";

            ObjectNode sys = messages.addObject();
            sys.put("author", "system");
            ArrayNode sysContent = sys.putArray("content");
            sysContent.addObject().put("type", "text").put("text", geminiSystemPrompt + "\n\nCurrent menu snapshot:\n" + menuContext);

            ObjectNode user = messages.addObject();
            user.put("author", "user");
            ArrayNode userContent = user.putArray("content");
            userContent.addObject().put("type", "text").put("text", message);

            payload = requestBody.toString();
        } catch (Exception ex) {
            return Mono.error(new IllegalStateException("Unable to prepare AI request for Gemini"));
        }

        return webClient.post()
            .uri(endpoint)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + geminiApiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchangeToMono(resp -> {
                        int code = resp.statusCode().value();
                if (resp.statusCode().is2xxSuccessful()) {
                return resp.bodyToMono(String.class).map(this::extractGeminiReply);
                }
                if (code == 401) {
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new IllegalStateException("401 Unauthorized: Invalid Gemini API key or missing permission" + (body.isBlank() ? "" : ": " + body))));
                }
                if (code == 403) {
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new IllegalStateException("403 Forbidden: Access to the Gemini model is denied" + (body.isBlank() ? "" : ": " + body))));
                }
                if (code == 404) {
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new IllegalStateException("404 Not Found: Gemini endpoint or model not found" + (body.isBlank() ? "" : ": " + body))));
                }
                if (code == 429) {
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new IllegalStateException("429 Rate Limit: Too many requests to Gemini" + (body.isBlank() ? "" : ": " + body))));
                }
                return resp.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> Mono.error(new IllegalStateException("AI provider request failed with status " + code + (body.isBlank() ? "" : ": " + body))));
            })
            .onErrorMap(ex -> !(ex instanceof IllegalStateException), ex -> new IllegalStateException("Unable to reach AI provider"));
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
                .exchangeToMono(resp -> {
                    int code = resp.statusCode().value();
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (resp.statusCode().is2xxSuccessful()) {
                                    return Mono.just(body).map(this::extractReply);
                                }
                                String errorMessage = "AI provider request failed with status " + code;
                                if (!body.isBlank()) {
                                    errorMessage += ": " + body;
                                }
                                return Mono.error(new IllegalStateException(errorMessage));
                            });
                })
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

            // Prefer the documented candidates -> content[] -> text path
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode candidate = candidates.get(0);
                JsonNode contentNode = candidate.path("content");
                StringBuilder out = new StringBuilder();
                if (contentNode.isArray()) {
                    for (JsonNode part : contentNode) {
                        if (part.has("text")) {
                            out.append(part.path("text").asText(""));
                        } else if (part.has("type") && part.has("text")) {
                            out.append(part.path("text").asText(""));
                        } else {
                            // fallback: collect any textual fields
                            if (part.isObject()) {
                                part.fields().forEachRemaining(e -> {
                                    if (e.getValue().isTextual()) out.append(e.getValue().asText()).append(' ');
                                });
                            }
                        }
                    }
                }
                String candidateText = out.toString().trim();
                if (!candidateText.isBlank()) return candidateText;
            }

            // Generic fallback: walk the JSON tree for the first text field
            JsonNode textNode = findFirstTextNode(root);
            if (textNode != null && textNode.isTextual() && !textNode.asText().isBlank()) {
                return textNode.asText().trim();
            }

            throw new IllegalStateException("AI provider returned an empty response");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse AI response");
        }
    }

    private JsonNode findFirstTextNode(JsonNode root) {
        if (root == null) return null;
        if (root.isTextual()) return root;
        if (root.isObject()) {
            for (java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                java.util.Map.Entry<String, JsonNode> e = it.next();
                JsonNode found = findFirstTextNode(e.getValue());
                if (found != null) return found;
            }
        } else if (root.isArray()) {
            for (JsonNode el : root) {
                JsonNode found = findFirstTextNode(el);
                if (found != null) return found;
            }
        }
        return null;
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
        // allow callers to provide an endpoint that already contains /v1
        if (normalizedEndpoint.endsWith("/v1")) {
            return normalizedEndpoint + "/models/" + URLEncoder.encode(geminiModel, StandardCharsets.UTF_8) + ":generateMessage";
        }
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
