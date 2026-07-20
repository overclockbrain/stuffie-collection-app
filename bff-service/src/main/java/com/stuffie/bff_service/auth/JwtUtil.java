package com.stuffie.bff_service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWTトークンの検証・読み取り専用ユーティリティ。
 * トークンの発行はstuffed-animal-service側が行うため、BFFは検証・読み取りのみを行う。
 * jwt.secret はstuffed-animal-serviceと同じ値をapplication.yamlで指定する必要がある
 * （署名の検証には発行時と同じ秘密鍵が必要なため）。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * トークンからクレームを取り出す。署名検証も同時に行われる。
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * トークンからメールアドレス（subject）を取り出す。
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * トークンが有効か（署名・期限切れ）を確認する。
     */
    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}