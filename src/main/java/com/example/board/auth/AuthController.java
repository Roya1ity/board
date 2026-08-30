package com.example.board.auth;

import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
    private final RefreshCookieFactory refreshCookieFactory;

    @Value("${jwt.refressh-token-validity-seconds}")
    private long refreshTokenValiditySeconds;

    @PostMapping("/new")
    public IngestResult create(@Valid @RequestBody SignupRequest req) {


        return authService.signUp(req);
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest req) {

        UserResponse res = authService.login(req);
        ResponseCookie cookie = refreshCookieFactory.create(res.getRefreshToken(),refreshTokenValiditySeconds);


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logdout(@CookieValue(name = "refreshToken",required = false) String refreshToken,
                                        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String accessToken = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring("Beare ".length());
        }
        if (refreshToken != null) {
            authService.logout(refreshToken,accessToken);
        }

        ResponseCookie cookie = refreshCookieFactory.expire();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .build();
    }

    @PostMapping("/reissue")
    public TokenResponse reIssue(@CookieValue(name = "refreshToken",required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException(ErrorCode.LOGIN_REQUIRED);
        }
        log.debug("refresh token in cookie: {}", refreshToken);
        return authService.reIssueToken(refreshToken);
    }
}
