

FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradle gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src/
RUN chmod +x ./gradlew && \
    ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]




