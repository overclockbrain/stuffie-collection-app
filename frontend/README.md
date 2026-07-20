# stuffie-collection-app frontend

ぬいぐるみ管理アプリのフロントエンド。React + TypeScript + Vite構成。

## セットアップ

```bash
npm install
npm run dev
```

`http://localhost:5173` で起動する。

## 前提

バックエンド（stuffed-animal-service）が `localhost:8081` で起動している必要がある。
`vite.config.ts` の `server.proxy` で `/api` を `localhost:8081` に転送する設定になっている。

## ディレクトリ構成

```
src/
├── api/            APIクライアント（axios）
├── components/     再利用可能なコンポーネント
├── pages/          画面単位のコンポーネント
├── styles/         CSSファイル（コンポーネント単位で分離）
├── App.tsx         ログイン状態による画面切り替え
└── main.tsx        エントリーポイント
```

## 主な機能

- ログイン・認証（JWT）
- ぬいぐるみの一覧・登録・編集・削除
- シリーズのプルダウン管理（追加・削除）
- ダブり登録の警告表示

## 詳細ドキュメント

- [ローカル開発環境の起動手順](../docs/infrastructure/LOCAL_DEV.md)
- [DB設計](../docs/design/DB.md)
- [API仕様](../docs/design/openapi.yaml)