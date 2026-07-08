# ── Estágio 1: build (compila com Maven + JDK 17) ──────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Baixa as dependências antes de copiar o código, pra cachear essa camada
# entre builds (só recompila as dependências se o pom.xml mudar)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Estágio 2: runtime (só o JRE + o jar, imagem final bem menor) ──────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/pokemon-duel-backend-1.0.0.jar app.jar

# A Render injeta a variável PORT automaticamente — o application.properties
# já está configurado pra ler ${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
