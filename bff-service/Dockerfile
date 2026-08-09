# このDockerfileも stuffed-animal-service 同様、
# リポジトリのルートディレクトリをビルドコンテキストにして実行すること。
#   docker build -f bff-service/Dockerfile -t bff-service .

# ─────────────────────────────────────────
# ビルドステージ
# ─────────────────────────────────────────
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

COPY proto ./proto

WORKDIR /app/bff-service

COPY bff-service/pom.xml .
COPY bff-service/.mvn .mvn
COPY bff-service/mvnw .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY bff-service/src ./src
RUN ./mvnw package -DskipTests -B

# ─────────────────────────────────────────
# 実行ステージ
# ─────────────────────────────────────────
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

COPY --from=build /app/bff-service/target/*.jar app.jar

# 8080: フロントエンドから受け付けるREST API
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
