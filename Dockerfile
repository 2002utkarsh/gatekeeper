# Stage 1: build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/gatekeeper-1.0.0.jar ./gatekeeper.jar
ENTRYPOINT ["java", "-jar", "/app/gatekeeper.jar"]