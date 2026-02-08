# ===================================================================
# STAGE 1: BUILD STAGE
# Uses Gradle wrapper to build the application
# ===================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install required packages for Gradle
RUN apk add --no-cache bash

# Copy Gradle wrapper and build files first (for Docker layer caching)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Give execution permission to gradlew
RUN chmod +x gradlew

# Download dependencies (cached if build files don't change)
RUN ./gradlew dependencies --no-daemon --quiet || true

# Copy source code
COPY src src

# Build the application (skip tests for faster builds; run tests separately in CI/CD)
RUN ./gradlew bootJar --no-daemon -x test

# Extract layers for faster startup (Spring Boot layered JAR)
RUN mkdir -p extracted && \
    java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# ===================================================================
# STAGE 2: RUNTIME STAGE
# Minimal JRE image for running the application
# ===================================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Labels for container metadata
LABEL maintainer="hoangtien2k3"
LABEL application="auth-jwt-springboot"
LABEL version="1.0.0"
LABEL description="JWT Authentication Service with Spring Boot"

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Install curl for health checks (optional, can be removed if not needed)
RUN apk add --no-cache curl tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone to Asia/Ho_Chi_Minh
ENV TZ=Asia/Ho_Chi_Minh
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copy extracted layers from builder stage (optimized for Docker layer caching)
COPY --from=builder --chown=appuser:appgroup /app/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/extracted/application/ ./

# Switch to non-root user
USER appuser:appgroup

# Expose application port
EXPOSE 8080

# JVM optimization flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start the application using layered JAR launcher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
