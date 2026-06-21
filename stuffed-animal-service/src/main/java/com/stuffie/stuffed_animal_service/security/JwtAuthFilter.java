package com.stuffie.stuffed_animal_service.security;

import com.stuffie.stuffed_animal_service.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * リクエストごとに1回だけ動く JWT 認証フィルター。
 * Authorization ヘッダーから Bearer トークンを取り出し、
 * 有効なら SecurityContext にユーザー情報をセットする。
 *
 * OncePerRequestFilter を継承することで
 * 同一リクエスト内で複数回実行される心配がない。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization ヘッダーがない or Bearer 形式でなければスルー
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " の7文字を除いてトークンだけ取り出す
        String token = authHeader.substring(7);

        // トークンが無効（期限切れ・署名エラー）ならスルー
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // トークンからメールアドレスとロールを取り出す
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractClaims(token).get("role", String.class);

        // DBにユーザーが存在する場合のみ認証情報をセット
        userRepository.findByEmail(email).ifPresent(user -> {
            // Spring Security が認識できる形式（ROLE_USER / ROLE_ADMIN）に変換
            var auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            // SecurityContext にセットすることでこのリクエストを「認証済み」にする
            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        filterChain.doFilter(request, response);
    }
}