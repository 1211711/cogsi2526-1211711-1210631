# COGSI CA5 - Detailed Docker Build Report

This document is part of the _COGSI_ (Configuration and Systems Management) class from the Software Engineering master’s course at _ISEP_ (Instituto Superior de Engenharia do Porto).

The goal of this assignment was to apply already known concepts used on previous assignment (CA3 and CA4) and apply them using docker, by containerizing both the Chat Server and the Tut REST Applications.  
Although the applications are quite different, the methods used to containerize them were very similar.

---

## 1. Chat Server

### 1.1 Image v1 - Multi-stage

This image builds the application entirely inside Docker. It Clones the repository, builds the application and runs it, all inside the container.
It's fully automated which means no manual steps required outside Docker.

```dockerfile
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
```

1. Steps
   2. `FROM openjdk:17.0.1-jdk-slim AS builder` - uses a lightweight Java JDK image for building.
      1. `slim` reduces size by removing unnecessary packages.
   3. `WORKDIR /app` - sets the working directory inside the container.
   4. `RUN apt update && apt install -y git` - installs Git to clone the repository (required only during build stage).
   5. `RUN git clone` - clones the project into the container
      1. This avoids the need of manual copy from the host machine.
   6. `WORKDIR /app/CA2/Part1/gradle_basic_demo-main` - moves to project directory for Gradle build.
   7. `RUN ./gradlew build -x test --no-daemon` - builds the JAR inside the container.
      1. `-x test` - skips the tests for speed
      2. `--no-daemon` - instructs gradle to run in a single process without starting a background daemon, ensuring clean, reproducible Docker builds.
   8. `FROM openjdk:17.0.1-jdk-slim` - new image for runtime
      1.  `slim` reduces size by removing unnecessary packages.
   9. `COPY --from=builder` - copies only the built JAR.
   10. `EXPOSE 59001` - declares the port the app listens to.
   11. `ENTRYPOINT` - starts the Java application with the specified main class.

Pros: Ensures reproducible builds; final image is small since build tools are not included.

Cons: Build might take longer because everything (Git clone + Gradle build) runs inside the container

### 1.2 Image v1-large - Single-stage build

Similar to v1, but **no multi-stage**: clones repo, builds, and runs in a single image.

```dockerfile
FROM openjdk:17.0.1-jdk-slim

WORKDIR /app

RUN apt update &&  \
    apt install -y git

# Clone and Build
RUN git clone https://github.com/1211711/cogsi2526-1211711-1210631.git /app

WORKDIR /app/CA2/Part1/gradle_basic_demo-main
RUN ./gradlew build -x test --no-daemon

WORKDIR /app
COPY /app/CA2/Part1/gradle_basic_demo-main/build/libs/*.jar app.jar

EXPOSE 59001

ENTRYPOINT ["java", "-cp", "app.jar", "basic_demo.ChatServerApp", "59001"]
```

This image is a lot larger than the multistage one because it uses a single-stage build that includes the entire cloned 
repository, gradle build tools, and all build files in the final image. 
V1 is a multi-stage build that only copies the final JAR into a clean runtime image, leaving all build dependencies behind.

### 1.3 Image v2 - Prebuilt JAR

This image uses a prebuilt JAR copied from the host into the container. 

The application must be built manually, `./gradlew build`, on the host beforehand, and Docker only packages and runs the artifact. 
It is not fully automated since a host build is required.

```dockerfile
FROM openjdk:17.0.1-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 59001

ENTRYPOINT ["java", "-cp", "app.jar", "basic_demo.ChatServerApp", "59001"]
```

1. Steps:
   1. `WORKDIR /app` - sets the working directory inside the container. 
   2. `COPY` - copies only the built JAR, from the host machine, that was previously generate using gradle.
   3. `EXPOSE 59001` - declares the port the app listens to.
   4. `ENTRYPOINT` - starts the Java application with the specified main class.

Although size wise, this image is very similar to V1, the need of manual steps (such as the build), decrease a lot the value
of this approach.

### 1.4 Comparison and Analysis

In order to build each image there was the need to run the following command:

- `docker build -t chat-server:{imageVersion} .`
  - `-t chat-server:{imageVersion}` → names and tags the image.
    - Image version examples: v1, v1-large, v2.
  - `.` → specifies the build context (current directory).

Also in order to run the container after building the image the docker run command was used:
- `docker run -d -p 59001:59001 chat-server:{imageVersion}`
  - `-d` - run in detached mode.
  - `-p host_port:container_port` - port mapping.

![Chat Server running](img/chatServerRunning.png)

| Version | Size | Automation                  | Size                                                                                       |
|---------|------|-----------------------------|--------------------------------------------------------------------------------------------|
| v1      | small | fully automated             | 636.62MB                                    
| v1-large| large | fully automated             | 1.12GB                           
| v2      | small | requires host machine build | 636.59MB 

As it can be seen that v1 and v2 are similar in size because both include only the runtime and the final application JAR, 
with build tools and source code discarded or never included. 

On the other hand v1-large is much larger because it keeps the full repository, gradle caches, and build tools in the final image.
