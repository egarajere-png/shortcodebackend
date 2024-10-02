ARG VERSION=0.0.1-SNAPSHOT
FROM openjdk:11
MAINTAINER "samuel.waithaka@abcthebank.com"

WORKDIR /app

COPY target/shortcode-0.0.1-SNAPSHOT.jar ./

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "-Djasypt.encryptor.password=jasyptkey", "./shortcode-0.0.1-SNAPSHOT.jar"]