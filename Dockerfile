FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY servicesAccounKey.json /app

COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:resolve

COPY src ./src

RUN ./mvnw package -DskipTests

EXPOSE 8080

ENV GOOGLE_APPLICATION_CREDENTIALS="servicesAccounKey.json"

CMD ["java", "-jar", "target/mi-aplicacion.jar"]
