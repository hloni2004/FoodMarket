package com.llburgers.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that converts service-layer exceptions into
 * consistent JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles JSR-303 / Jakarta Validation failures from {@code @Valid} annotated
     * controller parameters (e.g. password strength, @Email, @NotBlank).
     * Collects all field-level errors into a single readable message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Validation failed";
        log.debug("[VALIDATION] {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "A database constraint was violated. Please try again or contact support.";
        String detail = ex.getMostSpecificCause().getMessage();
        if (detail != null) {
            if (detail.contains("Key (phone)")) {
                msg = "Phone number already registered";
            } else if (detail.contains("Key (email)")) {
                msg = "Email address already registered";
            } else if (detail.contains("Key (")) {
                // extract field name from "Key (fieldName)=(value) already exists"
                int start = detail.indexOf("Key (") + 5;
                int end = detail.indexOf(")", start);
                if (end > start) {
                    msg = "Duplicate value for field: " + detail.substring(start, end);
                }
            } else if (detail.contains("violates not-null constraint")) {
                msg = "A required field is missing. Please check your input and try again.";
            } else if (detail.contains("violates foreign key constraint")) {
                msg = "A referenced record was not found. Please refresh and try again.";
            }
        }
        log.warn("[DATA_INTEGRITY] detail={}", detail);
        return buildResponse(HttpStatus.CONFLICT, msg);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the maximum allowed limit");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("[UNHANDLED] {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        // The `error` key carries the human-readable message so the React frontend's
        // AuthContext (which reads `data.error`) receives a useful string rather than
        // an HTTP status phrase like "Bad Request".
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", message);          // usable by frontend
        body.put("message", message);        // kept for API consumers using the old key
        return ResponseEntity.status(status).body(body);
    }
}
