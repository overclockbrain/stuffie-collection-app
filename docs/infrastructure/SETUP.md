# stuffie-collection-app 環境構築ドキュメント

## 前提環境

| 項目     | 値                  |
| -------- | ------------------- |
| OS       | Ubuntu 22.04 LTS    |
| メモリ   | 8GB以上推奨         |
| ディスク | 50GB以上空き推奨    |
| GitLab   | 18.x (セルフホスト) |

---

## 1. Gitの初期設定

```bash
git config --global user.name "Your Name"
git config --global user.email "your-email@example.com"
git config --global init.defaultBranch main
```

---

## 2. GitLab軽量化 (メモリ8GB以下の場合)

```bash
sudo vim /etc/gitlab/gitlab.rb
```

以下を設定：

```ruby
puma['worker_processes'] = 2
sidekiq['concurrency'] = 5
```

```bash
sudo gitlab-ctl reconfigure
```

メモリのavailableが2GB以上あることを確認：

```bash
free -h
# available が 2.0Gi 以上あればOK
```

---

## 3. k3sインストール

```bash
curl -sfL https://get.k3s.io | sh -

# 起動確認
sudo systemctl status k3s

# ノード確認
sudo k3s kubectl get nodes
```

### kubectlをsudoなしで使えるようにする

```bash
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $(id -u):$(id -g) ~/.kube/config
echo 'export KUBECONFIG=~/.kube/config' >> ~/.bashrc
source ~/.bashrc

# 確認
kubectl get nodes
```

---

## 4. GitLab Container Registry有効化

```bash
sudo vim /etc/gitlab/gitlab.rb
```

以下を設定（YOUR_SERVER_IPは実際のIPに変更）：

```ruby
registry_external_url 'http://YOUR_SERVER_IP:5050'
gitlab_rails['registry_enabled'] = true
```

```bash
sudo gitlab-ctl reconfigure

# 確認
sudo gitlab-ctl status | grep registry
```

---

## 5. GitLab Runnerインストール・登録

### インストール

```bash
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh" | sudo bash
sudo apt install gitlab-runner -y

# バージョン確認
gitlab-runner --version
```

### GitLabへの登録

1. GitLabの **Settings > CI/CD > Runners** からRegistration tokenを取得
2. 以下を実行（YOUR_TOKEN・YOUR_GITLAB_URLは実際の値に変更）：

```bash
sudo gitlab-runner register \
  --url http://YOUR_GITLAB_URL \
  --registration-token YOUR_TOKEN \
  --executor docker \
  --docker-image alpine:latest \
  --description "homeserver-runner" \
  --docker-privileged
```

### 登録確認

```bash
sudo gitlab-runner list
```

---

## 6. GitHubミラー設定

### GitHubでPersonal Access Tokenを生成

1. GitHub > **Settings > Developer settings > Personal access tokens > Tokens (classic)**
2. `repo` 権限にチェックして生成
3. トークンをコピーしておく

### GitLabでミラー設定

1. GitLab リポジトリ > **Settings > Repository > Mirroring repositories**
2. 以下を入力：
   - **Git repository URL**: `https://YOUR_GITHUB_TOKEN@github.com/YOUR_GITHUB_USERNAME/stuffie-collection-app.git`
   - **Mirror direction**: Push
   - **✅ Only mirror protected branches**
3. **Mirror repository** をクリック

---

## 7. GitLab CI/CD変数の設定

GitLab > **Settings > CI/CD > Variables** に以下を登録：

| 変数名               | 内容                        | Masked |
| -------------------- | --------------------------- | ------ |
| `KUBECONFIG_CONTENT` | `cat ~/.kube/config` の内容 | ✅      |

---

## 8. 動作確認チェックリスト

```bash
# k3s
kubectl get nodes                          # Ready になってるか

# GitLab Runner
sudo gitlab-runner list                    # Runnerが登録されてるか

# GitLab Registry
curl http://YOUR_SERVER_IP:5050/v2/        # 200が返ってくるか

# メモリ
free -h                                    # available 2GB以上あるか
```

---

## トラブルシューティング

### k3sが起動しない

```bash
sudo journalctl -u k3s -f
```

### GitLab Runnerがジョブを拾わない

```bash
sudo gitlab-runner verify
sudo systemctl restart gitlab-runner
```

### メモリ不足でk3s Podが落ちる

```bash
# リソース使用量確認
kubectl top nodes
kubectl top pods -A
```

---

## 9. ローカル開発環境のDB初期データ投入

`DataInitializer.java` により、Spring Boot起動時に自動でテストデータが投入される。  
`@Profile("dev")` がついているため、本番環境では動作しない。

### 自動投入の仕組み

1. Docker ComposeでPostgreSQL起動
2. Spring Boot起動 → JPAがテーブル自動作成
3. `DataInitializer`が起動時にテストユーザーを投入
4. 既にデータがある場合はスキップ

### テストユーザー

| ユーザー名 | メールアドレス    | パスワード  | ロール |
| ---------- | ----------------- | ----------- | ------ |
| testuser   | test@example.com  | password123 | USER   |
| admin      | admin@example.com | admin123!   | ADMIN  |