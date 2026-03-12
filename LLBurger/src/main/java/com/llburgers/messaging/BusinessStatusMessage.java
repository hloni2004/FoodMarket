package com.llburgers.messaging;

import java.util.UUID;

/**
 * Lightweight message placed on {@code llburger.business.opened} or
 * {@code llburger.business.closed} queue when an admin toggles business status.
 *
 * @param adminId       UUID of the admin who triggered the change
 * @param adminName     display name of the admin (for logging)
 * @param closedMessage optional message shown to customers when closing (null when opening)
 */
public record BusinessStatusMessage(
        UUID adminId,
        String adminName,
        String closedMessage) {}
