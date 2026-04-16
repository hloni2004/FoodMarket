package com.llburgers.controller;

import com.llburgers.dto.ChatRequest;
import com.llburgers.dto.ChatResponse;
import com.llburgers.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Mono<ResponseEntity<?>> chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request.message())
                .map(reply -> ResponseEntity.ok((Object) new ChatResponse(reply)))
                .onErrorResume(IllegalStateException.class, ex -> {
                    log.warn("[CHAT] Chat provider failure: {}", ex.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                            .body(Map.of("error", "Chat service is temporarily unavailable. Please try again.")));
                });
    }
}
