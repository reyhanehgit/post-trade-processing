FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/currency-pair-service-*.jar app.jar
EXPOSE 8889
ENTRYPOINT ["java", "-jar", "app.jar"]

