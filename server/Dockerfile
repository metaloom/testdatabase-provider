# syntax=docker/dockerfile:1
FROM eclipse-temurin:19-jre

ADD target/testdatabase-provider-server-0.0.1-SNAPSHOT.jar /server.jar

EXPOSE 8080/tcp

CMD ["java", "-jar", "/server.jar"]
