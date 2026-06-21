# stuffie-collection-app 🧸

自分が持っているぬいぐるみを一覧管理し、ダブりを防ぐためのWebアプリ。  
マイクロサービス構成・k3s・GitLab CI/CD を活用した学習用プロジェクト。

---

## 技術スタック

| レイヤー       | 技術                      |
| -------------- | ------------------------- |
| フロントエンド | React + TypeScript + Vite |
| BFF            | Spring Boot (WebFlux)     |
| バックエンド   | Spring Boot (Web + JPA)   |
| DB             | PostgreSQL                |
| インフラ       | k3s (Kubernetes)          |
| CI/CD          | GitLab CI/CD              |
| コンテナ       | Docker                    |

---

## ドキュメント

| ドキュメント                                                           | 内容                               |
| ---------------------------------------------------------------------- | ---------------------------------- |
| [docs/infrastructure/LOCAL_DEV.md](./docs/infrastructure/LOCAL_DEV.md) | ローカル開発環境の起動手順         |
| [docs/infrastructure/SETUP.md](./docs/infrastructure/SETUP.md)         | サーバー環境構築手順 (k3s・GitLab) |
| [docs/design/ARCHITECTURE.md](./docs/design/ARCHITECTURE.md)           | 全体アーキテクチャ設計             |
| [docs/design/DB.md](./docs/design/DB.md)                               | DB設計・テーブル定義               |
| [docs/design/openapi.yaml](./docs/design/openapi.yaml)                 | API仕様 (OpenAPI 3.0)              |

---

## ディレクトリ構成

```
stuffie-collection-app/
├── frontend/                 # React フロントエンド
├── bff-service/              # BFF (Spring Boot)
├── stuffed-animal-service/   # バックエンド (Spring Boot)
├── k8s/                      # Kubernetes マニフェスト
├── docs/                     # ドキュメント
│   ├── infrastructure/
│   └── design/
├── docker-compose.yml        # ローカル開発用
└── .gitlab-ci.yml            # CI/CD パイプライン
```

---

## クイックスタート

ローカルで動かす場合は [LOCAL_DEV.md](./docs/infrastructure/LOCAL_DEV.md) を参照。