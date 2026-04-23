# ─────────────────────────────────────────────────────────────────
#  Shared Dockerfile — copy this into every service folder
#  (eureka-server, user-service, product-service, order-service,
#   cart-service, payment-service, api-gateway)
#
#  Multi-stage build:
#    Stage 1 — Maven compiles and packages the JAR
#    Stage 2 — Slim JRE image runs only the JAR
#
#  Result: ~200 MB image instead of ~800 MB (no JDK, no Maven in runtime)
# ─────────────────────────────────────────────────────────────────

# ── Stage 1: Build ────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies separately — only re-downloaded when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Build the JAR (skip tests — run tests in CI, not here)
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Health check (Docker uses this — override port per service if needed)
HEALTHCHECK --interval=15s --timeout=5s --retries=8 --start-period=60s \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# JVM flags tuned for containers with limited RAM:
#   UseContainerSupport — respects Docker memory limits (not host RAM)
#   MaxRAMPercentage=70 — JVM heap = 70% of container memory limit
#   TieredStopAtLevel=1 — faster startup (less JIT compilation at boot)
#   urandom             — faster SecureRandom on Linux containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:TieredStopAtLevel=1", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]