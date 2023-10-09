FROM maven:3.8.4-jdk-11 as builder

ADD duckapi-backend-0.0.1-SNAPSHOT.jar duckapi-backend-docker.jar

ENTRYPOINT ["java", "-jar", "/duckapi-backend-docker.jar", "--spring.profiles.active=prod"]