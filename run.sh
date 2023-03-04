#!/bin/bash

docker build -t docker-java:searchengine .
docker run -d --name app --link db:db -p 8080:8080 docker-java:searchengine