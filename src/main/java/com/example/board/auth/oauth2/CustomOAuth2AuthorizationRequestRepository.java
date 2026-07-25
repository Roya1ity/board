package com.example.board.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // http://localhost:8099/oauth2/authorization/kakao

    public static final String COOKIE_NAME = "oauthRequest";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final ObjectMapper objectMapper;
    private final boolean cookieSecure;

    record StoredRequest(
            String state,
            String authorizationUri,
            String clientId,
            String redirectUri,
            Set<String> scopes,
            String registrationId
    ) {}

    public CustomOAuth2AuthorizationRequestRepository(
            ObjectMapper objectMapper,
            @Value("${app.refresh-cookie.secure}") boolean secure
    ) {
        this.objectMapper = objectMapper;
        this.cookieSecure = secure;
    }

    private ResponseCookie cookie(String value,Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME,value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private void expireCookie(HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE,cookie("",Duration.ZERO).toString());
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            return;
        }

        StoredRequest store = new StoredRequest(
                authorizationRequest.getState(),
                authorizationRequest.getAuthorizationUri(),
                authorizationRequest.getClientId(),
                authorizationRequest.getRedirectUri(),
                authorizationRequest.getScopes(),
                authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID)
        );

        try {
            String json = objectMapper.writeValueAsString(store);
            String value = Base64.getUrlEncoder().encodeToString(
                    json.getBytes(StandardCharsets.UTF_8));
            response.addHeader(HttpHeaders.SET_COOKIE,cookie(value,TTL).toString());
        }
        catch (Exception e) {
            throw new RuntimeException("인증 요청 쿠키 직렬화 실패");
        }
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = findCookie(request);
        if (cookie == null) {
            return null;
        }

        try {
            String json = new String(
                    Base64.getUrlDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8
            );

            StoredRequest store = objectMapper.readValue(json,StoredRequest.class);

            log.debug("=======redirect-uri=========: {}",store.redirectUri());

            return OAuth2AuthorizationRequest.authorizationCode()
                    .state(store.state())
                    .authorizationUri(store.authorizationUri())
                    .clientId(store.clientId())
                    .redirectUri(store.redirectUri())
                    .scopes(store.scopes())
                    .attributes(attrs -> attrs
                            .put(OAuth2ParameterNames.REGISTRATION_ID,store.registrationId()))
                    .build();
        }
        catch (Exception e) {
            log.warn("쿠키 복원 실패: {}",e.getMessage());
            return null;
        }
    }

    private Cookie findCookie(HttpServletRequest req) {
        if (req.getCookies() == null) {
            return null;
        }

        for(Cookie cookie : req.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest auth2AuthorizationRequest = loadAuthorizationRequest(request);
        expireCookie(response);
        return auth2AuthorizationRequest;
    }
}
