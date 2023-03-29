# syntax=docker/dockerfile:1
FROM eclipse-temurin:19-jre

ARG VERSION

COPY maven/ /opt

ADD target/testdatabase-provider-server-$VERSION.jar /server.jar

RUN mv /opt/*.jar /opt/server.jar

EXPOSE 8080/tcp

CMD ["java", "-jar", "/opt/server.jar"]
