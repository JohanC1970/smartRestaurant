WORKDIR /app

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
