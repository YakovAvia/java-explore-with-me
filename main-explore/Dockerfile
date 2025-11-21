FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
COPY main-explore/pom.xml main-explore/
COPY stats-service/pom.xml stats-service/
COPY stats-service/stats-dto/pom.xml stats-service/stats-dto/
COPY stats-service/stats-client/pom.xml stats-service/stats-client/
COPY stats-service/stats-server/pom.xml stats-service/stats-server/

COPY stats-service/stats-dto/src stats-service/stats-dto/src
COPY stats-service/stats-client/src stats-service/stats-client/src
COPY stats-service/stats-server/src stats-service/stats-server/src
COPY main-explore/src main-explore/src

RUN mvn clean install -pl main-explore -am -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "main-explore/target/main-explore-0.0.1-SNAPSHOT.jar"]