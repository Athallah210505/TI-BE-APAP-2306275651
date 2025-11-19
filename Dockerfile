# Production stage - JAR sudah di-build di CI/CD
FROM eclipse-temurin:21-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod

# Copy JAR file yang sudah di-build dari CI/CD
COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
