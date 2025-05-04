# Use Java 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar
COPY target/here-springboot-0.0.1-SNAPSHOT.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
