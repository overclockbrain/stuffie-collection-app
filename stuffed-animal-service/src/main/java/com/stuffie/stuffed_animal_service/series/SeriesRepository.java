package com.stuffie.stuffed_animal_service.series;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * series テーブルへのアクセスを担うリポジトリ。
 * JpaRepository を継承することで基本的なCRUDは自動で使える。
 */
public interface SeriesRepository extends JpaRepository<Series, Long> {

    /**
     * シリーズ名で検索する。
     * 登録時の重複チェックに使う。
     */
    Optional<Series> findByName(String name);

    /**
     * シリーズ名が既に登録済みかチェックする。
     */
    boolean existsByName(String name);
}