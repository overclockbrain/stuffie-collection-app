package com.stuffie.stuffed_animal_service.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 認証レスポンスの DTO。
 * 登録・ログイン成功時にフロントエンドへ返す。
 * フロントは accessToken を Authorization ヘッダーに付けて以降のリクエストを送る。
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    /** JWT アクセストークン */
    private String accessToken;

    /** トークンの種類（常に "Bearer"） */
    private String tokenType = "Bearer";

    /** アクセストークンの有効期限（秒）例: 3600 = 1時間 */
    private long expiresIn;

    /**
     * tokenType はデフォルト値（Bearer）をそのまま使うためのコンストラクタ。
     */
    public AuthResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}