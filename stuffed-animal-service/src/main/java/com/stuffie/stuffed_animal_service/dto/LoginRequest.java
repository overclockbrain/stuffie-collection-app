package com.stuffie.stuffed_animal_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ログインリクエストの DTO。
 * メールアドレスとパスワードのみ受け取る。
 */
@Data
public class LoginRequest {

    /** メールアドレス（必須・形式チェックあり） */
    @NotBlank
    @Email
    private String email;

    /** パスワード（必須） */
    @NotBlank
    private String password;
}