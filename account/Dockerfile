# ------------------------
# Stage 1: Build the application
# ------------------------
FROM gradle:jdk22-jammy AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar --no-daemon

# ------------------------
# Stage 2: Run the application
# ------------------------
FROM openjdk:22-slim
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 667
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=667"]
