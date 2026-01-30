FROM eclipse-temurin:25-jre-alpine

ARG JAR_FILE=app.jar

WORKDIR /app

COPY target/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]