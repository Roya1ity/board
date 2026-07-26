package com.example.board.Global.config;

import com.example.board.Global.Entity.User;
import com.example.board.auth.jwt.JwtAuthenticationFilter;
import com.example.board.auth.oauth2.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomOAuth2AuthorizationRequestRepository customOAuth2AuthorizationRequestRepository,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
            CustomOidcUserService customOidcUserService
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)              // CSRF 비활성화
                .formLogin(AbstractHttpConfigurer::disable)     // Spring Security Form Login 기능 사용하지 않음
                .httpBasic(AbstractHttpConfigurer::disable)     // Spring Security Form Login 기능 사용하지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 처리

                .authorizeHttpRequests(auth-> auth
                        // 공개 : 인증/회원가입/로그아웃/게시글 조회
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/oauth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/board/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/post/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/comment/**").permitAll()
                        // 인증시 요청가능
                        .requestMatchers(HttpMethod.GET,"/api/user/me").authenticated()
                        // 권한있어야 요청가능
//                        .requestMatchers(HttpMethod.POST,"/api/board/**").hasRole(User.Role.ADMIN.name())
//                        .requestMatchers(HttpMethod.PUT,"/api/board/**").hasRole(User.Role.ADMIN.name())
//                        .requestMatchers(HttpMethod.DELETE,"/api/board/**").hasRole(User.Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                                .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(customOAuth2AuthorizationRequestRepository))
                                .userInfoEndpoint(userInfo-> userInfo
                                        .userService(customOAuth2UserService)
                                        .oidcUserService(customOidcUserService)
                                        )
                                .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler(oAuth2LoginFailureHandler))

                // 401(Unauthorized), 403(Forbidden)
                .exceptionHandling(e->e
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
