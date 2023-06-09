ARG VERSION=0.0.1-SNAPSHOT
FROM openjdk:11.0.8-jre-slim
MAINTAINER "samuel.waithaka@abcthebank.com"

WORKDIR /app

COPY target/shortcode-0.0.1-SNAPSHOT.jar ./

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "./shortcode-0.0.1-SNAPSHOT.jar"]