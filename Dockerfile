FROM openjdk:17
LABEL maintainer="dev626@gmail.com"
ENV SERVER_PORT=8080
ENV DATABASE_NAME=search_engine
ENV URL=jdbc:postgresql://db:5432/${DATABASE_NAME}
WORKDIR /usr/src/java
COPY java/SearchEngine-1.0-SNAPSHOT.jar app.jar
COPY java/application.yaml application.yaml
ENTRYPOINT ["java","-Dserver.port=${SERVER_PORT}","-Dspring.datasource.url=${URL}","-jar","app.jar"]
EXPOSE ${SERVER_PORT}