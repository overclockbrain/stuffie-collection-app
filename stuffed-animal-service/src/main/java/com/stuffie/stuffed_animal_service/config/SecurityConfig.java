package com.stuffie.stuffed_animal_service.config;
import com.stuffie.stuffed_animal_service.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Spring Security の設定クラス。
 * JWT を使ったステートレス認証の設定をここで行う。
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /** 許可するオリジン（application.yaml の cors.allowed-origins から注入） */
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * セキュリティフィルターチェーンの設定。
     * - CORS 設定を適用
     * - CSRF 無効（REST API なのでセッションを使わないため不要）
     * - セッション管理をステートレスに（JWT で都度認証するため）
     * - /api/auth/** は認証不要（ログイン・登録エンドポイント）
     * - それ以外は全て認証必須
     * - JWT フィルターを UsernamePasswordAuthenticationFilter の前に挟む
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS設定を適用
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()       // 認証エンドポイントは全員OK
                .requestMatchers("/actuator/health").permitAll()   // ヘルスチェックも全員OK
                .anyRequest().authenticated()                      // それ以外は認証必須
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS設定。
     * フロントエンド（Vite）からのリクエストを許可する。
     * 許可オリジンは application.yaml で管理し、k3s環境ではConfigMapで上書きする。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // application.yaml から読み込んだオリジンを許可
        config.setAllowedOrigins(List.of(allowedOrigins));
        // 使用するHTTPメソッドを許可（OPTIONSはプリフライトリクエスト用）
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 全てのヘッダーを許可
        config.setAllowedHeaders(List.of("*"));
        // Cookieや認証情報の送信を許可
        config.setAllowCredentials(true);

        // 全エンドポイントにCORS設定を適用
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * パスワードのハッシュ化に BCrypt を使う。
     * BCrypt は Salt 付きで強度が高くパスワード保存のデファクトスタンダード。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}