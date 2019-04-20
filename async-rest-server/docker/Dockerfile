FROM java:openjdk-8-jre

MAINTAINER Vlad Bulimac <buli.vlad@gmail.com>

RUN mkdir -p /opt/async-rest-server/libs

COPY build/libs/async-rest-server-1.0.0-SNAPSHOT-fat.jar /opt/async-rest-server/libs/async-rest-server.jar

EXPOSE 8123

WORKDIR /opt/async-rest-server/libs

ENTRYPOINT ["java", "-jar", "./async-rest-server.jar"]