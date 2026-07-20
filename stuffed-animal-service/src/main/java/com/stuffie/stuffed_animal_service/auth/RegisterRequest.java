package com.stuffie.stuffed_animal_service.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ユーザー登録リクエストの DTO。
 * @Valid アノテーションと組み合わせてバリデーションが自動で動く。
 */
@Data
public class RegisterRequest {

    /** ユーザー名（必須・最大100文字） */
    @NotBlank
    @Size(max = 100)
    private String username;

    /** メールアドレス（必須・形式チェックあり） */
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /** パスワード（必須・8文字以上） */
    @NotBlank
    @Size(min = 8, max = 255)
    private String password;
}