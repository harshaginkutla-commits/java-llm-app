# ── Stage 1: Build ───────────────────────────────────────────────────────────
# Uses a full Maven + JDK image just to compile and package the app.
# This stage is thrown away after the build — nothing from it leaks into prod.
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first and download dependencies.
# Docker caches this layer — if pom.xml hasn't changed, deps aren't re-downloaded.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build the fat JAR (skip tests — CI runs them separately)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Slim JRE-only image — no Maven, no source, no build tools. Much smaller.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Non-root user for security (never run containers as root in production)
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Copy only the compiled JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Document the port (doesn't actually publish it — that's done at runtime)
EXPOSE 8080

# Health check so Kubernetes knows when the container is ready
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]
