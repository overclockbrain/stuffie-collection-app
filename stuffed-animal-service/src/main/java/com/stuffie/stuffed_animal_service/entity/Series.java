package com.stuffie.stuffed_animal_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * series テーブルに対応するエンティティクラス。
 * ぬいぐるみのシリーズ（例: ディズニー）をマスタ管理する。
 * ユーザーがプルダウンで自由に追加・削除できる。
 */
@Entity
@Table(name = "series")
@Data
@NoArgsConstructor
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** シリーズ名（一意・必須） */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** 登録ユーザー（必須・更新不可） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    /** 登録日時（自動セット・更新不可） */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 新規保存前に登録日時を自動セット */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}