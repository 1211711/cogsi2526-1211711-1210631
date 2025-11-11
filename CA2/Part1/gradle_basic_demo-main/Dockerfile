FROM openjdk:17.0.1-jdk-slim AS builder

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 59001

ENTRYPOINT ["java", "-cp", "app.jar", "basic_demo.ChatServerApp", "59001"]
