package com.stuffie.stuffed_animal_service.repository;

import com.stuffie.stuffed_animal_service.entity.StuffedAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * stuffed_animals テーブルへのアクセスを担うリポジトリ。
 * JpaRepository を継承することで基本的なCRUDは自動で使える。
 */
public interface StuffedAnimalRepository extends JpaRepository<StuffedAnimal, Long> {

    /**
     * 名前・シリーズ・キャラクターで絞り込み検索する。
     * 各条件はnullの場合スキップされる。
     */
    @Query("""
        SELECT a FROM StuffedAnimal a
        WHERE (:name IS NULL OR a.name LIKE %:name%)
          AND (:seriesId IS NULL OR a.series.id = :seriesId)
          AND (:character IS NULL OR a.character LIKE %:character%)
        ORDER BY a.createdAt DESC
        """)
    List<StuffedAnimal> findByFilters(
        @Param("name") String name,
        @Param("seriesId") Long seriesId,
        @Param("character") String character
    );

    /**
     * ダブりチェック用。
     * 名前・シリーズ・キャラクターが全て一致するものを返す。
     */
    @Query("""
        SELECT a FROM StuffedAnimal a
        WHERE a.name = :name
          AND (:seriesId IS NULL OR a.series.id = :seriesId)
          AND (:character IS NULL OR a.character = :character)
        """)
    List<StuffedAnimal> findDuplicates(
        @Param("name") String name,
        @Param("seriesId") Long seriesId,
        @Param("character") String character
    );
}