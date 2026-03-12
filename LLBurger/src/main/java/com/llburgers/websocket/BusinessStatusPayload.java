package com.llburgers.websocket;

import com.llburgers.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload pushed over WebSocket for business open/close events.
 *
 * <p>Sent on {@code /topic/business} – all connected clients receive this.</p>
 *
 * @param notificationId  the persisted {@link com.llburgers.domain.Notification} ID
 * @param type            BUSINESS_OPENED or BUSINESS_CLOSED
 * @param message         human-readable message (may include admin's closure note)
 * @param timestamp       when the event occurred
 */
public record BusinessStatusPayload(
        UUID notificationId,
        NotificationType type,
        String message,
        LocalDateTime timestamp) {}
