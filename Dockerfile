ARG VERSION=0.0.1-SNAPSHOT
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/shortcode-0.0.1-SNAPSHOT.jar ./

EXPOSE 8088

ENTRYPOINT ["java","-Djasypt.encryptor.password=jasyptkey","-jar","./shortcode-0.0.1-SNAPSHOT.jar"]