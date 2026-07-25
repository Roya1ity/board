package com.example.board.auth.oauth2;

import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.ErrorResponse;
import com.example.board.Global.exception.OAuth2DuplicateEmailException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {


        ErrorCode errorCode = ErrorCode.LOGIN_FAILED;

        if (exception instanceof OAuth2DuplicateEmailException oauth2Ex) {
            errorCode = ErrorCode.DUPLICATE_USER_EMAIL;
        }
        response.setStatus(errorCode.getStatus().value());
        response.setContentType((MediaType.APPLICATION_JSON_VALUE+";charset=UTF-8"));

        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
    }
}
