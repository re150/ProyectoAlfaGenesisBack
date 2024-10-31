FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

COPY servicesAccounKey.json /servicesAccounKey.json

COPY --from=build /target/BackAG-0.0.1-SNAPSHOT.jar BackAG.jar

EXPOSE 8080

ENV GOOGLE_APPLICATION_CREDENTIALS=/target/servicesAccounKey.json

ENTRYPOINT ["java", "-jar", "BackAG.jar"]
