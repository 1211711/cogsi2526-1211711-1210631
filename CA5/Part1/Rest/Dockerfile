FROM openjdk:17.0.1-jdk-slim AS builder

WORKDIR /app

RUN apt update &&  \
    apt install -y git

# Clone and Build
RUN git clone https://github.com/1211711/cogsi2526-1211711-1210631.git /app

WORKDIR /app
RUN git fetch
RUN git checkout bugfix/no-issue/fix_tut_rest_application_properties

WORKDIR /app/CA2/Part2/gradle-migration
RUN ./gradlew build -x test --no-daemon

# Setup the jar and build
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=builder /app/CA2/Part2/gradle-migration/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "8080"]