# ============================
# 1. Build Stage
# ============================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom + source
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn -e -X -DskipTests clean package

# ============================
# 2. Run Stage
# ============================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (matches Spring Boot)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
