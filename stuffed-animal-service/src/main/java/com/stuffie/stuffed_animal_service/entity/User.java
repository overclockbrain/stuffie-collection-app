package com.stuffie.stuffed_animal_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * users テーブルに対応するエンティティクラス。
 * ロールは USER（一般）と ADMIN（管理者）の2種類。
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ユーザー名（一意） */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /** メールアドレス（一意・ログインに使う） */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt ハッシュ済みパスワード（平文は保存しない） */
    @Column(nullable = false, length = 255)
    private String password;

    /** ロール: USER（デフォルト）または ADMIN */
    @Column(nullable = false, length = 20)
    private String role = "USER";

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