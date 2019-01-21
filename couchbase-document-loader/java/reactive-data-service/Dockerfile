FROM maven:latest

ADD . /app

WORKDIR /app

RUN mvn clean install -DskipTests

CMD ["java", "-jar", "target/reactive-data-service-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080
