FROM openjdk:17.0.1-jdk-slim AS builder

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "8080"]