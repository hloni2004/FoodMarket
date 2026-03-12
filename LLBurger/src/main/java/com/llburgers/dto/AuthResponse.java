package com.llburgers.dto;

/** Returned to the client after successful login or token refresh. */
public record AuthResponse(
    String accessToken,
    String tokenType,
    UserSummary user
) {
    public static AuthResponse of(String accessToken, UserSummary user) {
        return new AuthResponse(accessToken, "Bearer", user);
    }
}
