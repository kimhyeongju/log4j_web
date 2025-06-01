# Multi-stage build for optimization
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build the application
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# Runtime stage
FROM openjdk:17-jdk-slim

ENV JAVA_OPTS="-Dlog4j2.formatMsgNoLookups=false \
              -Dcom.sun.jndi.ldap.object.trustURLCodebase=true \
              -Dcom.sun.jndi.rmi.object.trustURLCodebase=true"

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar"]