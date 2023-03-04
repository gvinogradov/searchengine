FROM openjdk:17
LABEL maintainer="dev626@gmail.com"
ENV SERVER_PORT=8080
ENV DATASOURCE_URL=jdbc:mysql://192.168.31.205:3306/search_engine?useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true
WORKDIR /usr/src/java
COPY java/SearchEngine-1.0-SNAPSHOT.jar app.jar
COPY java/application.yaml application.yaml
ENTRYPOINT ["java","-Dserver.port=${SERVER_PORT}","-Dspring.datasource.url=${DATASOURCE_URL}","-jar","app.jar"]
EXPOSE ${SERVER_PORT}