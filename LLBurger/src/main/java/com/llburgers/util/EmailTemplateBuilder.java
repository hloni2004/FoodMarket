package com.llburgers.util;

public final class EmailTemplateBuilder {

    private static final String BRAND_NAME = "LL Burgers";

    // Brand palette aligned to frontend theme
    private static final String COLOR_BG = "#FBF5F0";
    private static final String COLOR_SURFACE = "#FFFFFF";
    private static final String COLOR_PRIMARY = "#1E3E0F";
    private static final String COLOR_PRIMARY_SOFT = "#2A5720";
    private static final String COLOR_ACCENT = "#C58C3A";
    private static final String COLOR_ACCENT_SOFT = "#F4E7D4";
    private static final String COLOR_TEXT = "#111111";
    private static final String COLOR_MUTED = "#555555";
    private static final String COLOR_SUBTLE = "#7A7A7A";
    private static final String COLOR_BORDER = "#DDD7D3";
    private static final String COLOR_DIVIDER = "#ECE6E1";
    private static final String COLOR_WARNING_BG = "#FFF6EB";
    private static final String COLOR_WARNING_BORDER = "#F0D2A7";

    private EmailTemplateBuilder() {
    }

    public static String welcome(String customerName, String block, String roomNumber) {
        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:34px;line-height:1.15;letter-spacing:-0.02em;color:%s;font-weight:800;">Welcome to %s</h1>
                <p style="margin:0 0 18px;font-size:16px;line-height:1.6;color:%s;">Hi %s, your account is now active and ready for your first order.</p>
                %s
                <div style="margin:18px 0 0;">
                  <p style="margin:0;font-size:14px;line-height:1.6;color:%s;">Tap the button below to explore what is cooking today.</p>
                </div>
                """.formatted(
                infoBadge("New Account", "You are all set"),
                COLOR_PRIMARY,
                BRAND_NAME,
                COLOR_MUTED,
                e(customerName),
                statCard("Delivery Setup", "Block " + e(block), "Room " + e(roomNumber)),
                COLOR_TEXT
        );

        return wrap("Welcome", content, "Explore Menu", "https://llburgers-prompt-art.vercel.app/menu");
    }

    public static String businessOpened(String customerName) {
        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:32px;line-height:1.15;color:%s;font-weight:800;">Kitchen is Open</h1>
                <p style="margin:0 0 14px;font-size:16px;line-height:1.6;color:%s;">Hi %s, your favorites are now available.</p>
                <div style="margin:0;padding:14px 16px;border:1px solid %s;border-radius:14px;background:%s;">
                  <p style="margin:0;font-size:14px;line-height:1.6;color:%s;">Fresh burgers and sides are ready. Place your order while the kitchen is hot.</p>
                </div>
                """.formatted(
                infoBadge("Now Serving", "Fast delivery available"),
                COLOR_PRIMARY,
                COLOR_TEXT,
                e(customerName),
                COLOR_BORDER,
                COLOR_BG,
                COLOR_TEXT
        );

        return wrap("Business Open", content, "Order Now", "https://llburgers-prompt-art.vercel.app/menu");
    }

    public static String businessClosed(String customerName, String closedMessage) {
        String note = (closedMessage == null || closedMessage.isBlank())
                ? ""
                : "<div style=\"margin:14px 0 0;padding:12px 14px;border:1px solid " + COLOR_WARNING_BORDER + ";border-radius:12px;background:"
                + COLOR_WARNING_BG + ";\"><p style=\"margin:0;font-size:14px;line-height:1.6;color:" + COLOR_TEXT
                + ";\"><strong>Note:</strong> " + e(closedMessage) + "</p></div>";

        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:32px;line-height:1.15;color:%s;font-weight:800;">Kitchen is Closed</h1>
                <p style="margin:0 0 12px;font-size:16px;line-height:1.6;color:%s;">Hi %s, we are closed for now.</p>
                %s
                <p style="margin:14px 0 0;font-size:14px;line-height:1.6;color:%s;">We will notify you as soon as we reopen.</p>
                """.formatted(
                infoBadge("Temporary Pause", "Service resumes soon"),
                COLOR_PRIMARY,
                COLOR_TEXT,
                e(customerName),
                note,
                COLOR_TEXT
        );

        return wrap("Business Closed", content, "Visit Website", "https://llburgers-prompt-art.vercel.app/");
    }

    public static String orderConfirmation(String customerName,
                                           String orderRef,
                                           String block,
                                           String room,
                                           String itemsHtml,
                                           String total,
                                           String specialInstructions) {

        String note = (specialInstructions == null || specialInstructions.isBlank())
                ? ""
                : "<div style=\"margin:14px 0 0;padding:12px 14px;border:1px solid " + COLOR_WARNING_BORDER + ";border-radius:12px;background:"
                + COLOR_WARNING_BG + ";\"><p style=\"margin:0;font-size:14px;line-height:1.6;color:" + COLOR_TEXT + ";\"><strong>Special instructions:</strong> "
                + e(specialInstructions) + "</p></div>";

        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:32px;line-height:1.15;color:%s;font-weight:800;">Order Confirmed</h1>
                <p style="margin:0 0 14px;font-size:16px;line-height:1.6;color:%s;">Hi %s, your order <strong>#%s</strong> is confirmed and queued for preparation.</p>
                %s
                <div style="height:14px;line-height:14px;">&nbsp;</div>
                %s
                %s
                <p style="margin:16px 0 0;font-size:20px;line-height:1.3;color:%s;font-weight:800;">Total: %s</p>
                <p style="margin:8px 0 0;font-size:13px;line-height:1.6;color:%s;">Delivery is free and there are no extra charges.</p>
                """.formatted(
                infoBadge("Order Placed", "Reference: #" + e(orderRef)),
                COLOR_PRIMARY,
                COLOR_TEXT,
                e(customerName),
                e(orderRef),
                statCard("Delivery", "Block " + e(block), "Room " + e(room)),
                itemsHtml,
                note,
                COLOR_PRIMARY,
                e(total),
                COLOR_MUTED
        );

        return wrap("Order Confirmation", content, "Track Orders", "https://llburgers-prompt-art.vercel.app/orders");
    }

    public static String orderStatusUpdate(String customerName,
                                           String orderRef,
                                           String status,
                                           String block,
                                           String room) {
        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:32px;line-height:1.15;color:%s;font-weight:800;">Order Status Updated</h1>
                <p style="margin:0 0 14px;font-size:16px;line-height:1.6;color:%s;">Hi %s, order <strong>#%s</strong> is now <strong>%s</strong>.</p>
                %s
                """.formatted(
                infoBadge("Live Status", "Real-time tracking enabled"),
                COLOR_PRIMARY,
                COLOR_TEXT,
                e(customerName),
                e(orderRef),
                e(status),
                statCard("Delivery", "Block " + e(block), "Room " + e(room))
        );

        return wrap("Order Status", content, "Open Orders", "https://llburgers-prompt-art.vercel.app/orders");
    }

    public static String otpCode(String customerName, String otp) {
        String content = """
                %s
                <h1 style="margin:0 0 10px;font-size:32px;line-height:1.15;color:%s;font-weight:800;">Password Reset Code</h1>
                <p style="margin:0 0 14px;font-size:16px;line-height:1.6;color:%s;">Hi %s, use this one-time code to reset your password.</p>
                <div style="margin:14px 0;padding:18px;border:1px dashed %s;border-radius:14px;background:%s;text-align:center;">
                  <span style="display:inline-block;font-size:34px;line-height:1.1;letter-spacing:8px;color:%s;font-weight:800;">%s</span>
                </div>
                <p style="margin:0;font-size:13px;line-height:1.6;color:%s;">This code expires in 10 minutes. Never share it with anyone.</p>
                """.formatted(
                infoBadge("Secure Verification", "One-time code"),
                COLOR_PRIMARY,
                COLOR_TEXT,
                e(customerName),
                COLOR_BORDER,
                COLOR_BG,
                COLOR_PRIMARY,
                e(otp),
                COLOR_MUTED
        );

        return wrap("OTP Code", content, "Open App", "https://llburgers-prompt-art.vercel.app/forgot-password");
    }

    public static String orderItemsTable(String itemsRowsHtml) {
        return """
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="border-collapse:separate;border-spacing:0;border:1px solid %s;border-radius:14px;overflow:hidden;">
                  <thead>
                    <tr style="background:%s;">
                      <th align="left" style="padding:12px 14px;font-size:12px;letter-spacing:0.05em;text-transform:uppercase;color:%s;">Item</th>
                      <th align="right" style="padding:12px 14px;font-size:12px;letter-spacing:0.05em;text-transform:uppercase;color:%s;">Qty</th>
                      <th align="right" style="padding:12px 14px;font-size:12px;letter-spacing:0.05em;text-transform:uppercase;color:%s;">Price</th>
                    </tr>
                  </thead>
                  <tbody>
                    %s
                  </tbody>
                </table>
                """.formatted(COLOR_BORDER, COLOR_BG, COLOR_SUBTLE, COLOR_SUBTLE, COLOR_SUBTLE, itemsRowsHtml);
    }

    public static String orderItemRow(String itemName, int quantity, String price) {
        return """
                <tr>
                  <td style="padding:12px 14px;font-size:14px;line-height:1.5;color:%s;border-top:1px solid %s;">%s</td>
                  <td align="right" style="padding:12px 14px;font-size:14px;line-height:1.5;color:%s;border-top:1px solid %s;">%s</td>
                  <td align="right" style="padding:12px 14px;font-size:14px;line-height:1.5;color:%s;border-top:1px solid %s;">%s</td>
                </tr>
                """.formatted(COLOR_TEXT, COLOR_BORDER, e(itemName), COLOR_TEXT, COLOR_BORDER, quantity, COLOR_TEXT, COLOR_BORDER, e(price));
    }

    private static String wrap(String previewTitle, String contentHtml, String ctaText, String ctaUrl) {
        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <meta name="x-apple-disable-message-reformatting">
                </head>
                <body style="margin:0;padding:0;background:%s;font-family:'DM Sans',Arial,sans-serif;color:%s;-webkit-font-smoothing:antialiased;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;">%s from %s</div>
                  <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="background:%s;padding:26px 10px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" cellpadding="0" cellspacing="0" width="620" style="max-width:620px;background:%s;border:1px solid %s;border-radius:20px;overflow:hidden;box-shadow:0 8px 26px rgba(30,62,15,0.10);">
                          <tr>
                            <td style="padding:0;">
                              <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="background:%s;background-image:linear-gradient(120deg,%s 0%%,%s 100%%);">
                                <tr>
                                  <td style="padding:24px 22px;">
                                    <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                      <tr>
                                        <td align="left" valign="middle">
                                          <p style="margin:0;color:#fff;font-size:12px;letter-spacing:0.12em;text-transform:uppercase;opacity:0.92;">%s</p>
                                          <p style="margin:8px 0 0;color:#fff;font-size:24px;line-height:1.15;font-weight:800;">%s</p>
                                        </td>
                                        <td align="right" valign="middle" style="width:110px;">
                                          <div style="display:inline-block;padding:8px 12px;background:rgba(255,255,255,0.15);border:1px solid rgba(255,255,255,0.28);border-radius:999px;color:#fff;font-size:12px;font-weight:700;letter-spacing:0.05em;text-transform:uppercase;">Premium</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:26px 22px 22px;">
                              %s
                              <table role="presentation" cellpadding="0" cellspacing="0" style="margin-top:24px;">
                                <tr>
                                  <td style="background:%s;background-image:linear-gradient(180deg,%s 0%%,%s 100%%);border-radius:999px;box-shadow:0 5px 14px rgba(30,62,15,0.30);">
                                    <a href="%s" style="display:inline-block;padding:12px 22px;color:#fff;text-decoration:none;font-size:14px;font-weight:800;letter-spacing:0.01em;">%s</a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 22px;border-top:1px solid %s;background:%s;">
                              <table role="presentation" cellpadding="0" cellspacing="0" width="100%%">
                                <tr>
                                  <td align="left">
                                    <p style="margin:0;font-size:12px;color:%s;">%s - Delivered to your door.</p>
                                  </td>
                                  <td align="right">
                                    <a href="https://llburgers-prompt-art.vercel.app" style="font-size:12px;color:%s;text-decoration:none;font-weight:700;">Visit Website</a>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                COLOR_BG,
                COLOR_TEXT,
                e(previewTitle),
                BRAND_NAME,
                COLOR_BG,
                COLOR_SURFACE,
                COLOR_BORDER,
                COLOR_PRIMARY,
                COLOR_PRIMARY,
                COLOR_PRIMARY_SOFT,
                previewTitle,
                BRAND_NAME,
                contentHtml,
                COLOR_PRIMARY,
                COLOR_PRIMARY_SOFT,
                COLOR_PRIMARY,
                e(ctaUrl),
                e(ctaText),
                COLOR_DIVIDER,
                COLOR_BG,
                COLOR_MUTED,
                BRAND_NAME,
                COLOR_ACCENT
        );
    }

    private static String infoBadge(String title, String subtitle) {
        return """
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 0 12px;">
                  <tr>
                    <td style="padding:8px 12px;border:1px solid %s;border-radius:999px;background:%s;">
                      <span style="font-size:11px;line-height:1;color:%s;font-weight:800;letter-spacing:0.06em;text-transform:uppercase;">%s</span>
                      <span style="font-size:11px;line-height:1;color:%s;margin-left:8px;">%s</span>
                    </td>
                  </tr>
                </table>
                """.formatted(COLOR_WARNING_BORDER, COLOR_ACCENT_SOFT, COLOR_PRIMARY, e(title), COLOR_MUTED, e(subtitle));
    }

    private static String statCard(String title, String line1, String line2) {
        return """
                <div style="margin:0;padding:14px 16px;border:1px solid %s;border-radius:14px;background:%s;">
                  <p style="margin:0 0 8px;font-size:12px;line-height:1;color:%s;font-weight:800;letter-spacing:0.05em;text-transform:uppercase;">%s</p>
                  <p style="margin:0;font-size:15px;line-height:1.5;color:%s;font-weight:700;">%s</p>
                  <p style="margin:2px 0 0;font-size:14px;line-height:1.5;color:%s;">%s</p>
                </div>
                """.formatted(COLOR_BORDER, COLOR_BG, COLOR_SUBTLE, e(title), COLOR_TEXT, e(line1), COLOR_MUTED, e(line2));
    }

    private static String e(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
