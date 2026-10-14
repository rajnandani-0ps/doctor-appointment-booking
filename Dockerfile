# Multi-stage Dockerfile for Cloud Deployment (Render, Railway, etc.)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and dependencies
COPY backend/mvnw backend/mvnw.cmd ./
COPY backend/.mvn ./.mvn
COPY backend/pom.xml ./
RUN chmod +x ./mvnw

# Copy source code and build production jar
COPY backend/src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/doctor-appointment-backend-1.0.0.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
