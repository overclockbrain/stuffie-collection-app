package com.stuffie.bff_service.auth;

import lombok.Data;

/**
 * フロントに返す認証レスポンスのDTO。
 * gRPCのAuthResponseと名前が被るため、REST用はAuthResponseDtoという名前にしている。
 */
@Data
public class AuthResponseDto {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
