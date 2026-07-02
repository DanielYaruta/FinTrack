# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy dependency descriptors first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Non-root user for security
RUN groupadd -r fintrack && useradd -r -g fintrack fintrack
USER fintrack

COPY --from=build /build/target/fintrack-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# SPRING_PROFILES_ACTIVE is passed via docker-compose env (not hardcoded here)
ENTRYPOINT ["java", "-jar", "app.jar"]
