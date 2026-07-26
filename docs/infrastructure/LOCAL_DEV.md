# ローカル開発環境 起動手順

## 前提条件

| ツール  | バージョン              |
| ------- | ----------------------- |
| Java    | 24以上                  |
| Node.js | LTS (18以上)            |
| Docker  | 最新                    |
| Maven   | mvnw (プロジェクト内包) |

---

## システム構成

```
React (localhost:5173)
   ↓ REST/JSON
bff-service (localhost:8080)
   ↓ gRPC (localhost:9091)
stuffed-animal-service (localhost:8081 / gRPC: 9091)
   ↓ JDBC
PostgreSQL (localhost:5432)
```

フロントは **bff-service（8080）** にのみアクセスする。
stuffed-animal-serviceへの通信はbff-serviceがgRPC経由で行うため、
フロントから直接8081を叩く必要はない。

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

RESTは8081、gRPCは9091で待ち受ける。

---

### ③ bff-service起動

**別ターミナルで実行**

```bash
cd stuffie-collection-app/bff-service
./mvnw spring-boot:run
```

以下が出たら起動完了：

```
Started BffServiceApplication in X seconds
```

**② のstuffed-animal-serviceが起動済みであることが前提**（gRPCで接続しにいくため）。
先にbff-serviceを起動すると、リクエスト時に接続エラーになることがある。

---

### ④ フロントエンド起動

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

`vite.config.ts` の proxy 設定で `/api` は **bff-service（8080）** に転送される。

---

## アクセス先

| サービス                      | URL                   | 役割                         |
| ----------------------------- | --------------------- | ---------------------------- |
| フロントエンド                | http://localhost:5173 | React UI                     |
| bff-service                   | http://localhost:8080 | フロント向けAPI窓口（REST）  |
| stuffed-animal-service (REST) | http://localhost:8081 | 直接は使わない（開発確認用） |
| stuffed-animal-service (gRPC) | localhost:9091        | bff-serviceからのみアクセス  |
| PostgreSQL                    | localhost:5432        | データベース                 |

---

## テストユーザー

| ユーザー名 | メールアドレス    | パスワード  | ロール |
| ---------- | ----------------- | ----------- | ------ |
| testuser   | test@example.com  | password123 | USER   |
| admin      | admin@example.com | admin123!   | ADMIN  |

---

## 動作確認（curl）

```bash
# ログイン（bff-service経由）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# ぬいぐるみ一覧取得（bff-service経由・トークン必須）
curl http://localhost:8080/api/stuffed-animals \
  -H "Authorization: Bearer <取得したトークン>"
```

---

## 停止手順

```bash
# フロントエンド・bff-service・stuffed-animal-service → 各ターミナルでCtrl+C

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

### フロントで401・一覧が取れない
- bff-serviceが起動しているか確認
- ブラウザの localStorage に古いトークンが残っていないか確認（`localStorage.clear()`）

### bff-serviceが起動時にgRPC接続エラーになる
- stuffed-animal-serviceが先に起動しているか確認（②→③の順番を守る）
- `stuffed-animal-service` の `application.yaml` の `grpc.server.port`（9091）と
  `bff-service` の `application.yaml` の `grpc.client.stuffed-animal-service.address` が
  一致しているか確認

### `relation "users" does not exist`
stuffed-animal-serviceを先に起動してテーブルを作ってな（Flywayが自動実行する）。

### ポート競合 (`Address already in use`)
```bash
# 例: 8080が使われてる場合
lsof -i :8080
kill -9 [PID]
```