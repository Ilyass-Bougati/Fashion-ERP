package com.sefault.server.security.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public ResponseCookie createAccessTokenCookie(String token, long durationInMillis) {
        return baseBuilder("access_token", token, "/")
                .maxAge(durationInMillis / 1000)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token, long durationInMillis) {
        return baseBuilder("refresh_token", token, "/api/v1/auth/refresh")
                .maxAge(durationInMillis / 1000)
                .build();
    }

    public ResponseCookie clearCookie(String name) {
        return baseBuilder(name, "", "/").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .path(path)
                .sameSite("Strict");
    }
}
