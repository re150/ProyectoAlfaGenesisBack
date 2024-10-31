FROM maven:3.9.6-openjdk-17 AS build

WORKDIR /app

COPY . /app

RUN mvn clean package

# Crear una nueva imagen basada en OpenJDK 17
FROM openjdk:17-jre-slim-buster

EXPOSE 8080

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/demo-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/demo-0.0.1-SNAPSHOT.jar"]