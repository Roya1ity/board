package com.example.board.auth;

import com.example.board.Global.Entity.User;
import com.example.board.Global.Entity.UserProfile;
import com.example.board.Global.IngestResult;
import com.example.board.Global.exception.DuplicateUserException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.NotFoundUserException;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.dto.LoginRequest;
import com.example.board.auth.dto.SignupRequest;
import com.example.board.auth.dto.UserResponse;
import com.example.board.auth.jwt.JwtTokenProvider;
import com.example.board.user.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.board.auth.jwt.JwtAuthenticationFilter.BEARER;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-validity-seconds}")
    private long accessTokenValiditySeconds;

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
        if (!userRepository.existsByEmail(req.getEmail())) {
            //return new IngestResult("error","해당 이메일 정보가 존재하지 않음");
        }
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new NotFoundUserException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getPw(),user.getPw())) {
            throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtTokenProvider.createToken(user.getId());

        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setNick(user.getNick());
        res.setRole(user.getRole().toString());
        res.setAccessToken(BEARER+accessToken);

        //return new IngestResult("OK","로그인 성공");
        return res;
    }

    public boolean logout() {

        return true;
    }
}
