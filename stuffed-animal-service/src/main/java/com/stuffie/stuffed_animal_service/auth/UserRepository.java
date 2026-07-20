package com.stuffie.stuffed_animal_service.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * users テーブルへのアクセスを担うリポジトリ。
 * JpaRepository を継承することで基本的な CRUD は自動で使える。
 * メソッド名からクエリを自動生成する Spring Data JPA の機能を活用している。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * メールアドレスでユーザーを検索する。
     * ログイン時に使う。
     */
    Optional<User> findByEmail(String email);

    /**
     * メールアドレスが既に登録済みかチェックする。
     * ユーザー登録時の重複チェックに使う。
     */
    boolean existsByEmail(String email);

    /**
     * ユーザー名が既に使われているかチェックする。
     * ユーザー登録時の重複チェックに使う。
     */
    boolean existsByUsername(String username);
}