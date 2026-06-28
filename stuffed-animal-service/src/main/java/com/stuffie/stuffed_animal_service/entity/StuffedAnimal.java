package com.stuffie.stuffed_animal_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * stuffed_animals テーブルに対応するエンティティクラス。
 * ぬいぐるみ1件分の情報を管理する。
 */
@Entity
@Table(name = "stuffed_animals")
@Data
@NoArgsConstructor
public class StuffedAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ぬいぐるみの名前（必須） */
    @Column(nullable = false, length = 100)
    private String name;

    /** シリーズ（プルダウン管理・任意） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    /** キャラクター名（任意） */
    @Column(length = 100)
    private String character;

    /** 購入日（任意） */
    private LocalDate purchaseDate;

    /** 購入場所（任意） */
    @Column(length = 100)
    private String purchasePlace;

    /** 画像URL（将来対応・任意） */
    @Column(length = 500)
    private String imageUrl;

    /** メモ・備考（任意） */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** 登録ユーザー（必須・更新不可） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    /** 更新ユーザー（必須） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    /** 登録日時（自動セット・更新不可） */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時（自動セット） */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 新規保存前に登録日時・更新日時を自動セット */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /** 更新前に更新日時を自動セット */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}