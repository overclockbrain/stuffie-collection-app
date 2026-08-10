package com.stuffie.stuffed_animal_service.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認証エンドポイントのコントローラー。
 * /api/auth/** は SecurityConfig で認証不要に設定してある。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * ユーザー登録。
     * 成功時は 201 Created と JWT トークンを返す。
     *
     * @param request username, email, password
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // @Valid でリクエストのバリデーション（NotBlank, Email等）を自動チェック
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * ログイン。
     * 成功時は 200 OK と JWT トークンを返す。
     *
     * @param request email, password
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}