FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/fidstp2-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

