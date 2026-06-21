# アーキテクチャ設計

## システム全体構成

```
自宅サーバー (Ubuntu 8GB)
│
├── GitLab (ホスト直インストール)
│   ├── Git リポジトリ
│   ├── GitLab CI/CD Pipeline
│   ├── GitLab Container Registry
│   └── Mirror → GitHub (ポートフォリオ用)
│
└── k3s クラスター
    └── stuffie-collection-app
        ├── frontend Pod (React :80)
        ├── bff Pod (Spring Boot :8080)
        ├── stuffed-animal-service Pod (Spring Boot :8081)
        └── PostgreSQL Pod (PersistentVolume付き)
```

## マイクロサービス構成

| サービス | 役割 | ポート | 技術 |
|---------|------|-------|------|
| frontend | UI | 80 | React + Vite + nginx |
| bff-service | API集約・フロント向け窓口 | 8080 | Spring Boot (WebFlux) |
| stuffed-animal-service | ぬいぐるみCRUD | 8081 | Spring Boot (Web + JPA) |
| postgres | データ永続化 | 5432 | PostgreSQL 16 |

## CI/CDパイプライン

```
push to main
    │
    ▼
┌────────┐   ┌─────────┐   ┌──────────────────────┐   ┌──────────┐
│  test  │ → │  build  │ → │  push to GitLab      │ → │  deploy  │
│ JUnit  │   │ Docker  │   │  Container Registry  │   │  to k3s  │
└────────┘   └─────────┘   └──────────────────────┘   └──────────┘
```

## ディレクトリ構成

```
stuffie-collection-app/
├── .gitlab-ci.yml
├── README.md
├── docs/
│   ├── infrastructure/
│   │   └── SETUP.md
│   └── design/
│       ├── ARCHITECTURE.md   # このファイル
│       ├── DB.md
│       └── openapi.yaml
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   └── src/
├── bff-service/
│   ├── Dockerfile
│   └── src/
├── stuffed-animal-service/
│   ├── Dockerfile
│   └── src/
└── k8s/
    ├── namespace.yaml
    ├── ingress.yaml
    ├── frontend/
    ├── bff/
    ├── stuffed-animal-service/
    └── postgres/
```

## 将来の拡張候補

| 機能 | 技術 |
|------|------|
| GitOps自動デプロイ | ArgoCD |
| 監視・アラート | Prometheus + Grafana |
| 画像アップロード | MinIO |
| 認証機能 | Keycloak |
| Helm Chart化 | Helm |
