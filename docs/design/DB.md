# DB設計

## テーブル一覧

| テーブル名        | 説明                              |
| ----------------- | --------------------------------- |
| `users`           | ユーザー情報                      |
| `series`          | シリーズマスタ (プルダウン管理用) |
| `stuffed_animals` | ぬいぐるみ情報                    |

---

## テーブル: `users`

| カラム名     | 型           | 制約                    | 日本語                |
| ------------ | ------------ | ----------------------- | --------------------- |
| `id`         | BIGSERIAL    | PRIMARY KEY             | ID                    |
| `username`   | VARCHAR(100) | NOT NULL UNIQUE         | ユーザー名            |
| `email`      | VARCHAR(255) | NOT NULL UNIQUE         | メールアドレス        |
| `password`   | VARCHAR(255) | NOT NULL                | パスワード (ハッシュ) |
| `role`       | VARCHAR(20)  | NOT NULL DEFAULT 'USER' | ロール (USER / ADMIN) |
| `created_at` | TIMESTAMP    | NOT NULL DEFAULT NOW()  | 登録日時              |
| `updated_at` | TIMESTAMP    | NOT NULL DEFAULT NOW()  | 更新日時              |

---

## テーブル: `series`

| カラム名     | 型           | 制約                    | 日本語       |
| ------------ | ------------ | ----------------------- | ------------ |
| `id`         | BIGSERIAL    | PRIMARY KEY             | ID           |
| `name`       | VARCHAR(100) | NOT NULL UNIQUE         | シリーズ名   |
| `created_by` | BIGINT       | NOT NULL, FK → users.id | 登録ユーザー |
| `created_at` | TIMESTAMP    | NOT NULL DEFAULT NOW()  | 登録日時     |

---

## テーブル: `stuffed_animals`

| カラム名         | 型           | 制約                    | 日本語       |
| ---------------- | ------------ | ----------------------- | ------------ |
| `id`             | BIGSERIAL    | PRIMARY KEY             | ID           |
| `name`           | VARCHAR(100) | NOT NULL                | 名前         |
| `series_id`      | BIGINT       | FK → series.id          | シリーズ     |
| `character`      | VARCHAR(100) |                         | キャラクター |
| `purchase_date`  | DATE         |                         | 購入日       |
| `purchase_place` | VARCHAR(100) |                         | 購入場所     |
| `image_url`      | VARCHAR(500) |                         | 画像         |
| `notes`          | TEXT         |                         | メモ         |
| `created_by`     | BIGINT       | NOT NULL, FK → users.id | 登録ユーザー |
| `updated_by`     | BIGINT       | NOT NULL, FK → users.id | 更新ユーザー |
| `created_at`     | TIMESTAMP    | NOT NULL DEFAULT NOW()  | 登録日時     |
| `updated_at`     | TIMESTAMP    | NOT NULL DEFAULT NOW()  | 更新日時     |

---

## アクセス制御

| 操作 | USER (自分のデータ) | USER (他人のデータ) | ADMIN |
| ---- | ------------------- | ------------------- | ----- |
| 閲覧 | ✅                   | ✅                   | ✅     |
| 登録 | ✅                   | -                   | ✅     |
| 更新 | ✅                   | ❌                   | ✅     |
| 削除 | ✅                   | ❌                   | ✅     |

> `created_by = ログイン中のユーザーID` で自分のデータか判定する。

---

## ER図

```
users
 │
 ├─< stuffed_animals (created_by, updated_by)
 │
 ├─< series (created_by)
 │
series
 │
 └─< stuffed_animals (series_id)
```

---

## ダブりチェックロジック

`name` + `series_id` + `character` の組み合わせが一致するものをダブりとみなす。

```sql
SELECT * FROM stuffed_animals
WHERE name      = :name
  AND series_id = :seriesId
  AND character = :character;
```

---

## DDL

```sql
-- ユーザー
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- シリーズマスタ
CREATE TABLE series (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_by  BIGINT       NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ぬいぐるみ
CREATE TABLE stuffed_animals (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    series_id      BIGINT       REFERENCES series(id),
    character      VARCHAR(100),
    purchase_date  DATE,
    purchase_place VARCHAR(100),
    image_url      VARCHAR(500),
    notes          TEXT,
    created_by     BIGINT       NOT NULL REFERENCES users(id),
    updated_by     BIGINT       NOT NULL REFERENCES users(id),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ダブりチェック用インデックス
CREATE INDEX idx_duplicate_check
    ON stuffed_animals (name, series_id, character);
```