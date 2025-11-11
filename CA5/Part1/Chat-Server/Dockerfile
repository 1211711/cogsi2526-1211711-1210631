FROM openjdk:17.0.1-jdk-slim AS builder

WORKDIR /app

RUN apt update &&  \
    apt install -y git

# Clone and Build
RUN git clone https://github.com/1211711/cogsi2526-1211711-1210631.git /app

WORKDIR /app/CA2/Part1/gradle_basic_demo-main
RUN ./gradlew build -x test --no-daemon

# Setup the jar and build
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=builder /app/CA2/Part1/gradle_basic_demo-main/build/libs/*.jar app.jar

EXPOSE 59001

ENTRYPOINT ["java", "-cp", "app.jar", "basic_demo.ChatServerApp", "59001"]
