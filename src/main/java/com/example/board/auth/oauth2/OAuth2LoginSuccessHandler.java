package com.example.board.auth.oauth2;

import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.*;
import com.example.board.auth.dto.UserResponse;
import com.example.board.auth.jwt.JwtTokenProvider;
import com.example.board.user.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Authenticator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

import static com.example.board.auth.jwt.JwtAuthenticationFilter.BEARER;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

        String provider = oauth2Token.getAuthorizedClientRegistrationId();
        String providerId = provider.toUpperCase(Locale.ROOT) + "_" + authentication.getName();

        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.LOGIN_FAILED));

        try {
            String accessToken = jwtTokenProvider.createToken(user.getEmail());
            TokenPair tokenPair = authService.issueRefreshTokenPair(user.getId());

            UserResponse res = new UserResponse();

            res.setId(user.getId());
            res.setEmail(user.getEmail());
            res.setNick(user.getNick());
            res.setRole(user.getRole().name());
            res.setAccessToken(BEARER+accessToken);
            res.setRefreshToken(tokenPair.getToken());

            response.setStatus(HttpServletResponse.SC_OK);
            response.addHeader(HttpHeaders.SET_COOKIE,refreshCookieFactory.create(tokenPair.getToken(),tokenPair.getExpireIn()).toString());
            response.sendRedirect("/");
        }
        catch (AuthenticationException e) {
            throw new UnauthorizedException(ErrorCode.LOGIN_REQUIRED);
        }



        log.debug("provider: {}",provider);
        log.debug("providerId: {}",providerId);
    }
}
