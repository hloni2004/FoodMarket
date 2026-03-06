package com.llburgers.event;

import com.llburgers.domain.Admin;

/**
 * Published when an admin opens the business.
 *
 * <p><b>Consumers:</b></p>
 * <ul>
 *     <li>{@code BusinessStatusEventListener} – sends "business opened" email
 *         to all active customers (Brevo) and creates notifications</li>
 * </ul>
 *
 * @param admin the admin who toggled the business open
 */
public record BusinessOpenedEvent(Admin admin) {}
