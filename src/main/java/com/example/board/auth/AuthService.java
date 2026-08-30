package com.example.board.auth;

import com.example.board.Global.Entity.RefreshToken;
import com.example.board.Global.Entity.User;
import com.example.board.Global.Entity.UserProfile;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.DuplicateUserException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.dto.LoginRequest;
import com.example.board.auth.dto.SignupRequest;
import com.example.board.auth.dto.TokenResponse;
import com.example.board.auth.dto.UserResponse;
import com.example.board.auth.jwt.JwtTokenProvider;
import com.example.board.user.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.board.auth.jwt.JwtAuthenticationFilter.BEARER;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisRefreshTokenStore redisRefreshTokenStore;
    private final RedisTokenDenylist redisTokenDenylist;

    @Value("${jwt.access-token-validity-seconds}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refressh-token-validity-seconds}")
    private long refreshTokenValiditySeconds;

    @Transactional
    public IngestResult signUp(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateUserException(ErrorCode.DUPLICATE_USER_EMAIL);
        }

        User user = User.fromInfo(req.getEmail(), passwordEncoder.encode(req.getPw()), req.getNick(), req.getRole());
        userRepository.save(user);

        if (!userProfileRepository.existsByUser(user)) {
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);

            userProfileRepository.save(userProfile);
        }

        IngestResult result = new IngestResult("OK","회원가입 완료");
        return result;
    }

    @Transactional
    public UserResponse login(LoginRequest req) {

        UserResponse res = new UserResponse();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(),req.getPw())
            );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.createToken(userDetails.getUsername());
            String refreshToken = issueRefreshToken(userDetails.getId());

            res.setId(userDetails.getId());
            res.setEmail(userDetails.getUsername());
            res.setNick(userDetails.getNick());
            res.setRole(userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .map(role -> role.replace("ROLE_", ""))
                    .orElse(null));
            res.setAccessToken(BEARER+accessToken);
            res.setRefreshToken(refreshToken);
        }
        catch (AuthenticationException e) {
            throw new UnauthorizedException(ErrorCode.LOGIN_REQUIRED);
        }



        //return new IngestResult("OK","로그인 성공");
        return res;
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        redisRefreshTokenStore.deleteByToken(refreshToken);
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            redisTokenDenylist.deny(
                    jwtTokenProvider.getJti(accessToken),
                    jwtTokenProvider.getRemainingSeconds(accessToken));
        }
    }

    @Transactional
    public String issueRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        redisRefreshTokenStore.save(userId,token,refreshTokenValiditySeconds);

//        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);
//        refreshTokenRepository.findByUserId(userId)
//        .ifPresentOrElse(
//                exist -> exist.update(token,expiresAt),
//                ()-> refreshTokenRepository.save((new RefreshToken(userId,token,expiresAt)))
//        );

        return token;
    }

    @Transactional
    public TokenPair issueRefreshTokenPair(Long userId) {
        String token = UUID.randomUUID().toString();
        redisRefreshTokenStore.save(userId,token,refreshTokenValiditySeconds);

//        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);
//        refreshTokenRepository.findByUserId(userId)
//                .ifPresentOrElse(
//                        exist -> exist.update(token,expiresAt),
//                        ()-> refreshTokenRepository.save((new RefreshToken(userId,token,expiresAt)))
//                );

        TokenPair tokenPair = new TokenPair();
        tokenPair.setToken(token);
        tokenPair.setExpireIn(refreshTokenValiditySeconds);
        return tokenPair;
    }

    @Transactional
    public TokenResponse reIssueToken(String refreshToken) {
//        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
//                .orElseThrow(()->new UnauthorizedException(ErrorCode.LOGIN_REQUIRED));
//        if (saved.isExpired()) {
//            refreshTokenRepository.deleteByToken(refreshToken);
//            throw new UnauthorizedException(ErrorCode.LOGIN_REQUIRED);
//        }
        Long userId = redisRefreshTokenStore.findUserId(refreshToken)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN));

        User user = userRepository.findById(userId)
                .orElseThrow(()->new UnauthorizedException(ErrorCode.LOGIN_REQUIRED));

        String newAccessToken = jwtTokenProvider.createToken(user.getEmail());

        TokenResponse res = new TokenResponse();
        res.setAccessToken(newAccessToken);
        res.setRefreshToken(refreshToken);
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setNick(user.getNick());
        res.setRole(user.getRole().name());

        return res;
    }
}
