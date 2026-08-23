package com.example.board.auth.oauth2;

import com.example.board.Global.Entity.User;
import com.example.board.Global.Entity.UserProfile;
import com.example.board.Global.exception.DuplicateUserException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.OAuth2DuplicateEmailException;
import com.example.board.auth.UserRepository;
import com.example.board.auth.jwt.JwtTokenProvider;
import com.example.board.user.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    private enum OAuth2Provider {
        KAKAO,
        GOOGLE;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        findOrCreate(registrationId,oauth2User.getAttributes());
        return oauth2User;
    }

    public void findOrCreate(String registrationId, Map<String, Object> attributes) {
        OAuth2Provider authProvider = OAuth2Provider.valueOf(registrationId.toUpperCase((Locale.ROOT)));
        String providerId = (authProvider == OAuth2Provider.KAKAO) ? attributes.get("id").toString() : attributes.get("sub").toString();
        log.debug("regist : {}",authProvider);
        log.debug("id : {}",providerId);
            User user = userRepository.findByProviderId(authProvider+"_"+providerId)
                    .orElseGet(() -> createUser(authProvider,attributes,providerId));

        userProfileRepository.findByUser(user).orElseGet(()->{
            UserProfile userProfile = new UserProfile();
            userProfile.setUser(user);

            return userProfileRepository.save(userProfile);
        });


    }

    private void verifySocialEmail(String email,String provider) {
        userRepository.findByEmailAndProvider(email,provider).ifPresent(
                (user) -> {
                    throw new OAuth2DuplicateEmailException();
                }
        );
    }

    @Transactional
    private User createUser(OAuth2Provider authProvider, Map<String, Object> attributes, String providerId) {
        User newUser = new User();
        String email = "";

        switch ( authProvider ) {
            case KAKAO -> {
                email = kakaoEmail(attributes);
                newUser.setNick(kakaoNickName(attributes));
                newUser.setProfileImageUrl(kakaoProfileImage(attributes));
            }
            case GOOGLE -> {
                email = attributes.get("email").toString();
                newUser.setNick(attributes.get("name").toString());
                newUser.setProfileImageUrl(attributes.get("picture").toString());
            }
        }

        verifySocialEmail(email,authProvider.toString());
        newUser.setEmail(email);
        newUser.setPw(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setProvider(authProvider.toString());
        newUser.setProviderId(authProvider.toString()+"_"+providerId);

        return userRepository.save(newUser);
    }

    private String kakaoNickName(Map<String, Object> attributes) {
        Object account = attributes.get("kakao_account");
        if (account == null) {
            return null;
        }

        Map<?,?> accountValue = (Map<?,?>) account;
        Map<?,?> accountProfile = (Map<?,?>) accountValue.get("profile");
        if (account == null) {
            return null;
        }
        return (String) accountProfile.get("nickname");
    }

    private String kakaoProfileImage(Map<String, Object> attributes) {
        Object account = attributes.get("kakao_account");
        if (account == null) {
            return null;
        }

        Map<?,?> accountValue = (Map<?,?>) account;
        Map<?,?> accountProfile = (Map<?,?>) accountValue.get("profile");
        if (account == null) {
            return null;
        }
        return (String) accountProfile.get("profile_image_url");
    }

    private String kakaoEmail(Map<String, Object> attributes) {
        Object account = attributes.get("kakao_account");
        if (account == null) {
            return null;
        }

        Map<?,?> accountValue = (Map<?,?>) account;
        return (String) accountValue.get("email");
    }
}

