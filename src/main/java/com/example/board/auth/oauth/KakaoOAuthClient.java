package com.example.board.auth.oauth;

import com.example.board.Global.exception.ErrorCode;
import com.example.board.Global.exception.UnauthorizedException;
import com.example.board.auth.oauth.dto.KaKaoTokenResponse;
import com.example.board.auth.oauth.dto.KakaoUserRespose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final KakaoOAuthProperties properties;
    private final RestClient restClient;

    public String authorizeUrl(String state) {
        String uri = UriComponentsBuilder.fromUriString(properties.getAuthorizeUri())
                .queryParam("client_id",properties.getAppkey())
                .queryParam("redirect_uri",properties.getCallback())
                .queryParam("response_type","code")
                .queryParam("state",state)
                .build()
                .toUriString();

        log.info("카카오 인증 URI: {}",uri);
        return uri;
    }

    public KaKaoTokenResponse requestToken(String code) {
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type","authorization_code");
        form.add("client_id", properties.getAppkey());
        form.add("client_secret", properties.getSecret());
        form.add("redirect_uri", properties.getCallback());
        form.add("code",code);

        try {
            KaKaoTokenResponse res = restClient.post()
                    .uri(properties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KaKaoTokenResponse.class);

            if (res == null || res.getAccessToken() == null) {
                throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
            }

            return res;
        }
        catch (RestClientException e) {
            throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
        }
    }

    public KakaoUserRespose fetchUserInfo(String accessToken) {
        try {
            KakaoUserRespose res = restClient.get()
                    .uri(properties.getUserInfo())
                    .header(HttpHeaders.AUTHORIZATION,"Bearer "+accessToken)
                    .retrieve()
                    .body(KakaoUserRespose.class);

            if (res == null || res.getId() == null) {
                throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
            }

            return res;
        }
        catch (RestClientException e) {
            throw new UnauthorizedException(ErrorCode.LOGIN_FAILED);
        }
    }
}
