FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/x-tcp-gateway-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]