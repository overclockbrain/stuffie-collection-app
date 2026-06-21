# ローカル開発環境 起動手順

## 前提条件

| ツール | バージョン |
|-------|-----------|
| Java | 24以上 |
| Node.js | LTS (18以上) |
| Docker | 最新 |
| Maven | mvnw (プロジェクト内包) |

---

## 起動手順

### ① PostgreSQL起動

```bash
cd stuffie-collection-app
docker compose up -d
```

確認：

```bash
docker compose ps
# postgres が Up になってればOK
```

---

### ② stuffed-animal-service起動

**別ターミナルで実行**

```bash
cd stuffie-collection-app/stuffed-animal-service
./mvnw spring-boot:run
```

以下が出たら起動完了：

```
Started StuffedAnimalServiceApplication in X seconds
```

初回起動時は自動でテストデータが投入される：

```
初期データの投入が完了しました
```

---

### ③ フロントエンド起動

**別ターミナルで実行**

```bash
cd stuffie-collection-app/frontend
nvm use --lts   # Node.jsのバージョンを切り替え
npm run dev
```

以下が出たら起動完了：

```
Local: http://localhost:5173/
```

---

## アクセス先

| サービス | URL |
|---------|-----|
| フロントエンド | http://localhost:5173 |
| stuffed-animal-service | http://localhost:8081 |
| PostgreSQL | localhost:5432 |

---

## テストユーザー

| ユーザー名 | メールアドレス | パスワード | ロール |
|-----------|--------------|-----------|-------|
| testuser | test@example.com | password123 | USER |
| admin | admin@example.com | admin123! | ADMIN |

---

## 停止手順

```bash
# フロントエンド・Spring Boot → 各ターミナルでCtrl+C

# PostgreSQL
docker compose down

# データも全部消したい場合
docker compose down -v
```

---

## よくあるエラー

### `nvm: command not found`
```bash
source ~/.zshrc
nvm use --lts
```

### `502 Bad Gateway`
stuffed-animal-serviceが起動してへん。ターミナル②を確認。

### `relation "users" does not exist`
Spring Bootを先に起動してテーブルを作ってな。

### ポート競合 (`Address already in use`)
```bash
# 8081が使われてる場合
lsof -i :8081
kill -9 [PID]
```
