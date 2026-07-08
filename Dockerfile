# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-23 AS build
WORKDIR /workspace

# Cache dependencies first for faster incremental builds.
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:23-jre

LABEL org.opencontainers.image.title="Locality Connector" \
      org.opencontainers.image.description="Spring Boot backend for the Locality Connector platform" \
      org.opencontainers.image.source="https://gitlab.com/your-group/locality-connector" \
      org.opencontainers.image.vendor="Locality Connector" \
      org.opencontainers.image.licenses="MIT"

# curl is used by the container HEALTHCHECK below.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/target/localityconnector-0.0.1-SNAPSHOT.jar app.jar

# Run as a non-root user.
RUN useradd -r -u 1001 appuser
USER appuser

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC" \
    SERVER_PORT=8081

EXPOSE 8081

# Surface application + Firebase health to the orchestrator.
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl -fsS "http://localhost:${SERVER_PORT}/health" || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
