FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/counterparty-service-*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

