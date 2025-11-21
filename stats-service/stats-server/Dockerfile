FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY stats-service/pom.xml stats-service/
COPY stats-service/stats-dto/pom.xml stats-service/stats-dto/
COPY stats-service/stats-client/pom.xml stats-service/stats-client/
COPY stats-service/stats-server/pom.xml stats-service/stats-server/

COPY stats-service/stats-dto/src stats-service/stats-dto/src
COPY stats-service/stats-client/src stats-service/stats-client/src
COPY stats-service/stats-server/src stats-service/stats-server/src

RUN mvn clean install -pl stats-service/stats-server -am -DskipTests

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "stats-service/stats-server/target/stats-server-0.0.1-SNAPSHOT.jar"]