package com.example.board.auth.oauth;

import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.RefreshCookieFactory;
import com.example.board.auth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/oauth/kakao")
public class KaKaoOAuthController {
    public static final String STATE_COOKIE_NAME = "oauthState";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final KakaoOAuthService kakaoOAuthService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final boolean cookieSecure;
    private final Long refreshTokenValiditySeconds;




    public KaKaoOAuthController(
            KakaoOAuthService kakaoOAuthService,
            RefreshCookieFactory refreshCookieFactory,
            @Value("${app.refresh-cookie.secure}") boolean secure,
            @Value("${jwt.refressh-token-validity-seconds}") long expireTokenSec
    ) {
        this.kakaoOAuthService = kakaoOAuthService;
        this.refreshCookieFactory = refreshCookieFactory;
        this.cookieSecure = secure;
        this.refreshTokenValiditySeconds = expireTokenSec;
    }


    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String state = UUID.randomUUID().toString();
        String cookie = stateCookie(state,STATE_TTL).toString();
        log.info("KakaoOAuthController.login:cookie: {}",cookie);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(kakaoOAuthService.authorizeUrl(state)))
                .header(HttpHeaders.SET_COOKIE,cookie)
                .build();
    }

    private ResponseCookie stateCookie(String value,Duration maxAge) {

        return ResponseCookie.from(STATE_COOKIE_NAME,value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/oauth/kakao")
                .maxAge(maxAge)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<UserResponse> callback(
            @RequestParam(name = "code") String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String state,
            @CookieValue(name = STATE_COOKIE_NAME, required = false) String stateCookie
    ) {
        if (error != null || code == null) {
            throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
        }
        if (stateCookie == null) {
            throw new UnauthorizedException(ErrorCode.INVALID_OAUTH_STATE);
        }

        UserResponse res = kakaoOAuthService.login(code);
        ResponseCookie cookie = refreshCookieFactory.create(res.getRefreshToken(),refreshTokenValiditySeconds);

        return ResponseEntity.ok().body(res);
    }
}
