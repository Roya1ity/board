package com.example.board.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieFactory {

    private final String name;
    private final boolean secure;
    private final String sameSite;
    private final String path;

    public RefreshCookieFactory(
            @Value("${app.refresh-cookie.name}") String name,
            @Value("${app.refresh-cookie.secure}") boolean secure,
            @Value("${app.refresh-cookie.same-site}") String sameSite,
            @Value("${app.refresh-cookie.path}") String path
    ) {
        this.name = name;
        this.secure = secure;
        this.sameSite = sameSite;
        this.path = path;
    }

    public String cookieName() {
        return name;
    }

    public ResponseCookie create(String refreshToken, long maxAgeSec) {
        return ResponseCookie.from(name,refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAgeSec)
                .build();
    }

    public ResponseCookie expire() {
        return ResponseCookie.from(name,"")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(0)
                .build();
    }
}
