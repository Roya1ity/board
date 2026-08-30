package com.example.board.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.KeyPair;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessTokenValiditySeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-token-validity-seconds}") long accessSeconds
    ) {
       this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode((base64Secret)));
       this.accessTokenValiditySeconds = accessSeconds;
       log.debug("JwtTokenProvider 생성됨: {}",base64Secret);
    }

    public String createToken(String userEmail) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(userEmail)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public io.jsonwebtoken.Claims parserClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserEmail(String token) {
        String subject = parserClaims(token).getSubject();

        log.debug("토큰으로부터 ID 추출: {}",subject);

        return subject;
    }

    public String getJti(String token) {
        return parserClaims(token).getId();
    }

    public long getRemainingSeconds(String token) {
        long remainMillis = parserClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Math.max(1,remainMillis/1000);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            log.debug("유효한 토큰: {}",token);
            return true;
        }
        catch (Exception e) {
            log.debug("올바르지 않은 토큰: {}",token);
            return false;
        }
    }
}
