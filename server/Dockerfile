# syntax=docker/dockerfile:1
FROM eclipse-temurin:19-jre

COPY maven/ /opt
RUN mv /opt/*.jar /opt/server.jar

EXPOSE 8080/tcp

CMD ["java", "-jar", "/opt/server.jar"]
