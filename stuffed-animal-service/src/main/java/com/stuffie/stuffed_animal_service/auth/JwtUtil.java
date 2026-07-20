package com.stuffie.stuffed_animal_service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT トークンの生成・検証を担うユーティリティクラス。
 * トークンの発行・解析・有効期限チェックをここで一元管理する。
 */
@Component
public class JwtUtil {

    /** application.yaml の jwt.secret から注入される署名キー */
    @Value("${jwt.secret}")
    private String secret;

    /** application.yaml の jwt.expiration から注入されるトークン有効期限（秒） */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * シークレットキーを HMAC-SHA 形式の SecretKey に変換する。
     * トークンの署名・検証に使う。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT トークンを生成する。
     *
     * @param email ユーザーのメールアドレス（subject に設定）
     * @param role  ユーザーのロール（USER / ADMIN）
     * @return 署名済み JWT トークン文字列
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)       // カスタムクレームとしてロールを埋め込む
                .issuedAt(new Date())      // トークン発行日時
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * トークンからクレーム（ペイロード）を取り出す。
     * 署名検証も同時に行われ、不正なトークンは例外が発生する。
     *
     * @param token JWT トークン文字列
     * @return クレーム情報
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * トークンからメールアドレスを取り出す。
     *
     * @param token JWT トークン文字列
     * @return メールアドレス
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * トークンが有効かどうかを確認する。
     * 署名不正・期限切れの場合は false を返す。
     *
     * @param token JWT トークン文字列
     * @return 有効なら true
     */
    public boolean isTokenValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            // 署名エラー・期限切れ等は全て無効扱い
            return false;
        }
    }

    /**
     * 有効期限（秒）を返す。
     * レスポンスの expiresIn フィールドに使う。
     */
    public long getExpiration() {
        return expiration;
    }
}