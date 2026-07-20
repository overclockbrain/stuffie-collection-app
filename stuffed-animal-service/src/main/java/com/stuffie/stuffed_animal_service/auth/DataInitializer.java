package com.stuffie.stuffed_animal_service.auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 開発環境専用の初期データ投入クラス。
 * @Profile("dev") によって本番環境では動かない。
 * Spring Boot起動時に自動実行される。
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // 既にデータがある場合はスキップ
        if (userRepository.count() > 0) {
            log.info("初期データは既に存在するためスキップします");
            return;
        }

        // テストユーザー登録
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("USER");
        userRepository.save(testUser);

        // 管理者ユーザー登録
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin123!"));
        adminUser.setRole("ADMIN");
        userRepository.save(adminUser);

        log.info("初期データの投入が完了しました");
    }
}