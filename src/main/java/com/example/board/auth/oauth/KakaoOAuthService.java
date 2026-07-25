package com.example.board.auth.oauth;

import com.example.board.Global.Entity.User;
import com.example.board.auth.AuthService;
import com.example.board.auth.UserRepository;
import com.example.board.auth.dto.UserResponse;
import com.example.board.auth.jwt.JwtTokenProvider;
import com.example.board.auth.oauth.dto.KaKaoTokenResponse;
import com.example.board.auth.oauth.dto.KakaoUserRespose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.board.auth.jwt.JwtAuthenticationFilter.BEARER;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoOAuthService {

    private static final String KAKAO_PREFIX = "KAKAO_";
    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    public String authorizeUrl(String state) {
        return kakaoOAuthClient.authorizeUrl(state);
    }

    public UserResponse login(String code) {
        KaKaoTokenResponse tokenResponse = kakaoOAuthClient.requestToken(code);
        //log.debug("Kakao Access Token: {}",tokenResponse.getAccessToken());

        KakaoUserRespose res = kakaoOAuthClient.fetchUserInfo(tokenResponse.getAccessToken());
        log.debug("Kakao User Email: {}",res.getEmail());

        User user = findOrCreateUser(res);
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = authService.issueRefreshToken(user.getId());

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNick(user.getNick());
        response.setAccessToken(BEARER+accessToken);
        response.setRefreshToken(refreshToken);

        return response;
    }

    private User findOrCreateUser(KakaoUserRespose kakaoUser) {
        String providerId = KAKAO_PREFIX + String.valueOf(kakaoUser.getId());
        return userRepository.findByProviderId(providerId)
                .orElseGet(()->createUser(kakaoUser,providerId));
    }

    private User createUser(KakaoUserRespose kakaoUser,String providerId) {
        String email = kakaoUser.getEmail();
        String pw = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = new User();
        user.setEmail(email);
        user.setPw(pw);
        user.setNick(kakaoUser.getNickname());
        user.setProfileImageUrl(kakaoUser.getProfileImageUrl());
        user.setProvider("KAKAO");
        user.setProviderId(providerId);

        return userRepository.save(user);

    }
}
