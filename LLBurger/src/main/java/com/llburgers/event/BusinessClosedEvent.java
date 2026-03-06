package com.llburgers.event;

import com.llburgers.domain.Admin;

/**
 * Published when an admin closes the business.
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>{@code BusinessStatusEventListener} – sends "business closed" email
 *         to all active customers (Brevo) and creates notifications</li>
 * </ul>
 *
 * @param admin         the admin who toggled the business closed
 * @param closedMessage optional human-readable reason for closing
 */
public record BusinessClosedEvent(Admin admin, String closedMessage) {}
