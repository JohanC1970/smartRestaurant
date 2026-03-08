# Etapa de build con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de runtime con Java 21
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto (Spring Boot usa 8080 por defecto)
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]