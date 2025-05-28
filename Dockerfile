# Dockerfile
FROM openjdk:17-jdk-slim

ENV JAVA_OPTS="-Dlog4j2.formatMsgNoLookups=false \
               -Dcom.sun.jndi.ldap.object.trustURLCodebase=true \
               -Dcom.sun.jndi.rmi.object.trustURLCodebase=true \
               -Dspring.profiles.active=prod"

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
