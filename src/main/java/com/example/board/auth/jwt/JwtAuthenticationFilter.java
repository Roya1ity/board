package com.example.board.auth.jwt;

import com.example.board.auth.AuthController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("JwtAuthenticationFilter::doFilterInternal 이 실행됨");

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            request.setAttribute(AuthController.LOGIN_USER_ID,jwtTokenProvider.getUserId(token));
            log.debug("JwtAuthenticationFilter::doFilterInternal: {}: {}",AuthController.LOGIN_USER_ID,request.getAttribute(AuthController.LOGIN_USER_ID));
        }
        filterChain.doFilter(request,response);
    }

    private String resolveToken(HttpServletRequest req) {
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
            return header.substring(BEARER.length());
        }

        return null;
    }
}
