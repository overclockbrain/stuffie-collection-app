package com.stuffie.stuffed_animal_service.service;

import com.stuffie.stuffed_animal_service.dto.AuthResponse;
import com.stuffie.stuffed_animal_service.dto.LoginRequest;
import com.stuffie.stuffed_animal_service.dto.RegisterRequest;
import com.stuffie.stuffed_animal_service.entity.User;
import com.stuffie.stuffed_animal_service.repository.UserRepository;
import com.stuffie.stuffed_animal_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認証に関するビジネスロジックを担うサービスクラス。
 * ユーザー登録・ログインの処理をここで行い、JWT を返す。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * ユーザー登録処理。
     * メールアドレス・ユーザー名の重複チェック後、パスワードをハッシュ化して保存する。
     *
     * @param request 登録リクエスト（username, email, password）
     * @return JWT アクセストークンと有効期限
     * @throws IllegalArgumentException メールアドレスまたはユーザー名が重複している場合
     */
    public AuthResponse register(RegisterRequest request) {
        // メールアドレスの重複チェック
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に登録されています");
        }
        // ユーザー名の重複チェック
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("このユーザー名は既に使われています");
        }

        // パスワードを BCrypt でハッシュ化して保存（平文では絶対に保存しない）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // 登録完了後にそのままトークンを返してフロントがすぐ使えるようにする
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, jwtUtil.getExpiration());
    }

    /**
     * ログイン処理。
     * メールアドレスでユーザーを検索し、パスワードを照合する。
     * どちらが間違っていても同じエラーメッセージを返す（ユーザー存在の特定を防ぐため）。
     *
     * @param request ログインリクエスト（email, password）
     * @return JWT アクセストークンと有効期限
     * @throws IllegalArgumentException 認証失敗の場合
     */
    public AuthResponse login(LoginRequest request) {
        // メールアドレスでユーザーを検索
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが違います"));

        // BCrypt でハッシュ比較（平文パスワードとハッシュを照合）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが違います");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, jwtUtil.getExpiration());
    }
}