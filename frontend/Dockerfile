# このDockerfileは frontend/ フォルダ単体をビルドコンテキストにして実行する。
#   docker build -f frontend/Dockerfile -t frontend ./frontend

# ─────────────────────────────────────────
# ビルドステージ
# ─────────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ─────────────────────────────────────────
# 実行ステージ（nginxで静的ファイルを配信）
# ─────────────────────────────────────────
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
