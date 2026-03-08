# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
# Copy the maven wrapper and pom file
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Ensure mvnw is executable
RUN chmod +x mvnw
# Download dependencies (optional but speeds up builds)
RUN ./mvnw dependency:go-offline -B
# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Optimize for Render Free Tier (512MB RAM)
# -Xmx256m is safer to avoid OOM kills by Render
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx256m -Djava.security.egd=file:/dev/./urandom -jar app.jar --server.port=${PORT:-8080}"]
