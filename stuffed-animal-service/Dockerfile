# このDockerfileは必ずリポジトリのルートディレクトリを
# ビルドコンテキストにして実行すること（../proto を参照するため）。
#   docker build -f stuffed-animal-service/Dockerfile -t stuffed-animal-service .
#
# docker-compose.yml から使う場合は自動的にルートコンテキストになるので気にしなくてよい。

# ─────────────────────────────────────────
# ビルドステージ
# ─────────────────────────────────────────
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

# サービス間で共有しているgRPCの定義ファイル
COPY proto ./proto

WORKDIR /app/stuffed-animal-service

# 依存関係の解決を先に済ませてDockerのレイヤーキャッシュを効かせる
COPY stuffed-animal-service/pom.xml .
COPY stuffed-animal-service/.mvn .mvn
COPY stuffed-animal-service/mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# ソースコードをコピーしてビルド（この時点で ../proto からコード生成される）
COPY stuffed-animal-service/src ./src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────
# 実行ステージ
# ─────────────────────────────────────────
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

COPY --from=build /app/stuffed-animal-service/target/*.jar app.jar

# 8081: REST（内部確認用途）, 9091: gRPC（bff-serviceからの通信用）
EXPOSE 8081 9091

ENTRYPOINT ["java", "-jar", "app.jar"]
